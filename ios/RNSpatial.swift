import SQLite3

@objc(RNSpatial)
class RNSpatial: NSObject {
  var handle: OpaquePointer? // Variable para el puntero de la base de datos

  // Método para conectar a la base de datos
   @objc(connect:withResolver:withRejecter:)
    func connect(_ paramsDataBase: [String: Any], resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
        guard let dbName = (paramsDataBase["dbName"] as? String)?.trimmingCharacters(in: .whitespaces), !dbName.isEmpty else {
            reject("DB_ERROR", "DBName can't be empty", nil)
            return
        }

        if let projDbPath = Bundle.main.resourcePath {
        setenv("PROJ_LIB", projDbPath, 1)
        print("PROJ_LIB set to: \(projDbPath)")
        } else {
        print("Error: proj.db directory not found in bundle")
        }

        let finalDbName = dbName.hasSuffix(".sqlite") ? dbName : dbName + ".sqlite"
        let filePath = finalDbName.cString(using: .utf8)

        // Abrir la base de datos SQLite
        if sqlite3_open_v2(filePath, &handle, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, nil) != SQLITE_OK {
            reject("DB_ERROR", "Unable to open database", nil)
            return
        }

        // Llamar a la función C para inicializar SpatiaLite
        if initialize_spatialite(handle) != 0 {
            reject("SPATIALITE_ERROR", "Failed to initialize SpatiaLite", nil)
            return
        }

        let result: [String: Any] = [
            "isConnected": true,
            "isSpatial": true
        ]
        resolve(result)
    }

  // Método para ejecutar consultas SQL
 @objc(executeQuery:withResolver:withRejecter:)
func executeQuery(_ query: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
    guard let handle = handle else {
        reject("DB_ERROR", "No database connection", nil)
        return
    }
    var stmt: OpaquePointer? = nil
    let result = sqlite3_prepare_v2(handle, query, -1, &stmt, nil)
    if result != SQLITE_OK {
        if let errorMessage = String(validatingUTF8: sqlite3_errmsg(handle)) {
            reject("QUERY_ERROR", "Failed to prepare query: \(errorMessage)", nil)
        }
        return
    }
    
    var results: [[String: Any]] = []
    
    // Ejecuta la consulta y procesa los resultados
    while sqlite3_step(stmt) == SQLITE_ROW {
        var row: [String: Any] = [:]
        let columnCount = sqlite3_column_count(stmt)
        
        for i in 0..<columnCount {
            let columnName = String(cString: sqlite3_column_name(stmt, i)).lowercased()
            let columnType = sqlite3_column_type(stmt, i)
            
            switch columnType {
            case SQLITE_INTEGER:
                let intValue = sqlite3_column_int64(stmt, i)
                row[columnName] = Int(intValue)
            case SQLITE_FLOAT:
                let doubleValue = sqlite3_column_double(stmt, i)
                row[columnName] = Double(doubleValue)
            case SQLITE_TEXT:
                if let textValue = sqlite3_column_text(stmt, i) {
                    row[columnName] = String(cString: textValue)
                } else {
                    row[columnName] = NSNull()
                }
            case SQLITE_NULL:
                row[columnName] = NSNull()
            default:
                if let textValue = sqlite3_column_text(stmt, i) {
                    row[columnName] = String(cString: textValue)
                } else {
                    row[columnName] = NSNull()
                }
            }
        }
        results.append(row)
    }
    
    sqlite3_finalize(stmt)
    let formattedResult: [String: Any] = ["data": results]
    resolve(formattedResult)
    }
}





