package com.fncspatialite

import android.os.Environment
import android.widget.Toast
import com.facebook.react.bridge.*
import java.util.Map
import java.io.File;
import jsqlite.Database
import jsqlite.Constants
import jsqlite.Stmt
import java.util.HashMap;

class RNSpatialModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var db: Database? = null
    private var isConnected = false
    private var docDir: String? = null

    override fun getName(): String {
        return NAME
    }

    @ReactMethod
fun connect(paramsDataBase: ReadableMap, promise: Promise) {
    try {
        val dbName = paramsDataBase.getString("dbName")?.trim().orEmpty()
        if (dbName.isEmpty()) {
            promise.reject("DBName can't be empty!", NullPointerException())
            return
        }

        val map = Arguments.createMap()
        db = Database()

        docDir = if (paramsDataBase.hasKey("localPath")) {
            val localPath = paramsDataBase.getString("localPath")?.trim().orEmpty()
            if (localPath.isEmpty()) {
                promise.reject("Local path can't be empty!", "Local path can't be empty!")
                return
            }
            val mainPath = Environment.getExternalStorageDirectory()
            val directory = File("$mainPath/$localPath") 
            if (!directory.isDirectory) directory.mkdirs()
            directory.absolutePath
        } else {
            reactApplicationContext.getExternalFilesDir(null)?.absolutePath
        }

        val isReadonly = paramsDataBase.hasKey("readonly") && paramsDataBase.getBoolean("readonly")
        val isSpatial = !paramsDataBase.hasKey("spatial") || paramsDataBase.getBoolean("spatial")

        val flags = if (isReadonly)
            Constants.SQLITE_OPEN_READONLY
        else
            Constants.SQLITE_OPEN_READWRITE or Constants.SQLITE_OPEN_CREATE

        db?.open(dbName, flags)
        // Check spatial initialized
        var spatialInitialized = false
        if (isSpatial) {
            try {
                spatialInitialized = db?.prepare("SELECT count(1) FROM spatial_ref_sys LIMIT 1")?.step() ?: false
            } catch (e: jsqlite.Exception) {
                if (e.message?.trim()?.startsWith("no such table: spatial_ref_sys") == true) {
                    db?.exec("SELECT InitSpatialMetaData(1)", null)
                }
            }
        }

        isConnected = true
        map.putBoolean("isConnected", isConnected)
        map.putBoolean("isSpatial", spatialInitialized)
        promise.resolve(map)

    } catch (e: Exception) {
        promise.reject(e.message, e)
    }
}


    @ReactMethod
    fun close(promise: Promise) {
        try {
            db?.close()
            isConnected = false
            val map = Arguments.createMap()
            map.putBoolean("isConnected", isConnected)
            promise.resolve(map)
        } catch (e: jsqlite.Exception) {
            promise.reject(e.message, e)
        }
    }

    @ReactMethod
    fun executeQuery(query: String, params: ReadableArray, promise: Promise) {
        var stmt: Stmt? = null
        try {
            stmt = db?.prepare(query)
            if (stmt == null) {
                promise.reject("Statement preparation failed", "Could not prepare statement")
                return
            }
            // Bind parameters
            for (i in 0 until params.size()) {
                when (params.getType(i)) {
                    ReadableType.String -> stmt.bind(i + 1, params.getString(i))
                    ReadableType.Number -> stmt.bind(i + 1, params.getDouble(i))
                    ReadableType.Boolean -> stmt.bind(i + 1, if (params.getBoolean(i)) 1 else 0)
                    ReadableType.Null -> stmt.bind(i + 1, null as String?)
                    else -> stmt.bind(i + 1, null as String?)
                }
            }

            val rows = Arguments.createArray()
            val colCount = stmt.column_count()
            val colNames = Array(colCount) { idx -> stmt.column_name(idx).toLowerCase() }
            var rowCount = 0

            while (stmt.step()) {
                rowCount++
                val row = Arguments.createMap()
                for (i in 0 until colCount) {
                    when (stmt.column_type(i)) {
                        Constants.SQLITE3_TEXT -> row.putString(colNames[i], stmt.column_string(i))
                        Constants.SQLITE_INTEGER -> row.putInt(colNames[i], stmt.column_long(i).toInt())
                        Constants.SQLITE_FLOAT -> row.putDouble(colNames[i], stmt.column_double(i))
                        Constants.SQLITE_NULL -> row.putNull(colNames[i])
                        else -> row.putString(colNames[i], stmt.column_string(i))
                    }
                }
                rows.pushMap(row)
            }

            val result = Arguments.createMap().apply {
                putInt("rows", rowCount)
                putInt("cols", colCount)
                putArray("data", rows)
            }
            promise.resolve(result)
        } catch (e: jsqlite.Exception) {
            promise.reject(e.message, e)
        } finally {
            try {
                stmt?.close()
            } catch (_: Exception) {}
        }
    }

// ...existing code...

    companion object {
        const val NAME = "RNSpatial"
    }
}
