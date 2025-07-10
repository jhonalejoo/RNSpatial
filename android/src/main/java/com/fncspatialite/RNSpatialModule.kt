package com.fncspatialite

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Arguments
import android.os.Environment
import android.widget.Toast
import java.util.Map
import java.io.File
import jsqlite.Database
import jsqlite.Constants
import jsqlite.Stmt
import java.util.HashMap

@ReactModule(name = RNSpatialModule.NAME)
class RNSpatialModule(reactContext: ReactApplicationContext) :
  NativeRNSpatialSpec(reactContext) {

  private var db: Database? = null
  private var isConnected = false
  private var docDir: String? = null

  override fun getName(): String {
    return NAME
  }


  @ReactMethod
  override fun connect(paramsDataBase: ReadableMap, promise: Promise) {
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
  override fun close(promise: Promise) {
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
  override fun executeQuery(query: String, promise: Promise) {
    try {
        val stmt = db?.prepare(query)
        val rows = Arguments.createArray()
        var rowCount = 0
        var colCount = 0

        while (stmt?.step() == true) {
            rowCount++
            if (colCount == 0) {
                colCount = stmt.column_count()
            }
            val row = Arguments.createMap()
            for (i in 0 until colCount) {
                when (stmt.column_type(i)) {
                    Constants.SQLITE3_TEXT -> row.putString(stmt.column_name(i).toLowerCase(), stmt.column_string(i))
                    Constants.SQLITE_INTEGER -> row.putInt(stmt.column_name(i).toLowerCase(), stmt.column_long(i).toInt())
                    Constants.SQLITE_FLOAT -> row.putDouble(stmt.column_name(i).toLowerCase(), stmt.column_double(i))
                    Constants.SQLITE_NULL -> row.putNull(stmt.column_name(i).toLowerCase())
                    else -> row.putString(stmt.column_name(i).toLowerCase(), stmt.column_string(i))
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
    }
  }

  companion object {
    const val NAME = "RNSpatial"
  }
}
