//
//  RNSpatial.c
//  RNSpatial
//
//  Created by Jhon Alejandro Cuervo Sanchez on 4/10/24.
//

#include <sqlite3.h>
#include <spatialite.h>
#include <spatialite/gaiageo.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int initialize_spatialite(sqlite3 *handle) {
    void *cache;
    char *errMsg = NULL;
    int ret;

    // Inicializar SpatiaLite
    cache = spatialite_alloc_connection();
    spatialite_init_ex(handle, cache, 1);

    // Verificar si la tabla spatial_ref_sys existe
    const char *checkSpatialTable = "SELECT count(1) FROM spatial_ref_sys LIMIT 1;";
    sqlite3_stmt *stmt;
    ret = sqlite3_prepare_v2(handle, checkSpatialTable, -1, &stmt, NULL);

    if (ret != SQLITE_OK) {
        if (sqlite3_errmsg(handle) && strstr(sqlite3_errmsg(handle), "no such table: spatial_ref_sys") != NULL) {
            // Inicializar metadata espacial si no existe
            const char *initSpatial = "SELECT InitSpatialMetaData(1);";
            ret = sqlite3_exec(handle, initSpatial, 0, 0, &errMsg);
            if (ret != SQLITE_OK) {
                printf("Error al inicializar SpatiaLite: %s\n", errMsg);
                sqlite3_free(errMsg);
                return -1;
            }
        }
    }

    sqlite3_finalize(stmt);
    return 0;  // Inicialización exitosa
}

