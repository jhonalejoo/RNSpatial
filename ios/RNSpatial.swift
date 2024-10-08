@objc(RNSpatial)
class RNSpatial: NSObject {
  var handle: OpaquePointer? // Definir la variable 'handle' aquí

  @objc(connect:withResolver:withRejecter:)
  func connect(_ paramsDataBase: [String: Any], resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
    guard let dbName = (paramsDataBase["dbName"] as? String)?.trimmingCharacters(in: .whitespaces), !dbName.isEmpty else {
        reject("DB_ERROR", "DBName can't be empty", nil)
        return
    }

    // Aquí procesas dbName y otros parámetros como lo haces en Android.
    let finalDbName = dbName.hasSuffix(".sqlite") ? dbName : dbName + ".sqlite"
    
    // Pasa el nombre de archivo al método `myopen` en C
    let filePath = finalDbName.cString(using: .utf8)
    let spatialInitialized = myopen(filePath)

    if spatialInitialized == -1 {
        reject("DB_ERROR", "Failed to open database or initialize SpatiaLite", nil)
    } else {
        let result: [String: Any] = [
            "isConnected": true,
            "isSpatial": spatialInitialized == 1
        ]
        resolve(result)
    }
}

@objc(executeQuery:withResolver:withRejecter:)
func executeQuery(_ query: String, resolver resolve: RCTPromiseResolveBlock, rejecter reject: RCTPromiseRejectBlock) {
    
    // Ejecutar la consulta usando la función C
    var errMsg: UnsafeMutablePointer<CChar>?
    var columnNames: UnsafeMutablePointer<UnsafeMutablePointer<CChar>?>?
    var rows: UnsafeMutablePointer<UnsafeMutablePointer<CChar>?>?
    var rowCount: Int32 = 0
    var colCount: Int32 = 0

    let result = execute_query(handle, query, &errMsg, &columnNames, &rows, &rowCount, &colCount)

    if result != SQLITE_OK {
        if let errMsg = errMsg {
            reject("QUERY_ERROR", String(cString: errMsg), nil)
            sqlite3_free(errMsg)
        } else {
            reject("QUERY_ERROR", "Unknown error", nil)
        }
        return
    }

    // Construir el resultado como un mapa y array
    var resultsArray: [[String: Any]] = []

    for rowIndex in 0..<Int(rowCount) {
        var rowDict: [String: Any] = [:]
        for colIndex in 0..<Int(colCount) {
            let columnName = String(cString: columnNames![colIndex]!)
            let value = String(cString: rows![rowIndex * Int(colCount) + colIndex]!)
            rowDict[columnName] = value
        }
        resultsArray.append(rowDict)
    }

    // Devolver el resultado a través de la promesa
    let resultDict: [String: Any] = [
        "rows": rowCount,
        "cols": colCount,
        "data": resultsArray
    ]

    resolve(resultDict)

    // Liberar la memoria
    sqlite3_close(handle)
  }

}
