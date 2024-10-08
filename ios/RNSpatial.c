//
//  RNSpatial.c
//  RNSpatial
//
//  Created by Jhon Alejandro Cuervo Sanchez on 4/10/24.
//

#include "RNSpatial.h"
#include <sqlite3.h>
#include <spatialite.h>
#include <spatialite/gaiageo.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int myopen(const char *filename) {
    int ret;
    sqlite3 *handle;
    void *cache;
    char *errMsg = NULL;
    int spatial_initialized = 0;

  printf("Error al inicializar SpatiaLite: %s\n", filename);

    /* Abrir la conexión a la base de datos */
    ret = sqlite3_open_v2(filename, &handle, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE, NULL);
    if (ret != SQLITE_OK) {
        printf("No se puede abrir '%s': %s\n", filename, sqlite3_errmsg(handle));
        sqlite3_close(handle);
        return -1;
    }

    /* Inicializar SpatiaLite */
    cache = spatialite_alloc_connection();
    spatialite_init_ex(handle, cache, 1);

    /* Verificar si la tabla spatial_ref_sys existe */
    const char *checkSpatialTable = "SELECT count(1) FROM spatial_ref_sys LIMIT 1;";
    sqlite3_stmt *stmt;
    ret = sqlite3_prepare_v2(handle, checkSpatialTable, -1, &stmt, NULL);
    
    if (ret == SQLITE_OK) {
        if (sqlite3_step(stmt) == SQLITE_ROW) {
            spatial_initialized = 1;  // SpatiaLite ya está inicializado
        }
    } else {
        if (sqlite3_errmsg(handle) && strstr(sqlite3_errmsg(handle), "no such table: spatial_ref_sys") != NULL) {
            /* Inicializar metadata espacial si no existe */
            const char *initSpatial = "SELECT InitSpatialMetaData(1);";
            ret = sqlite3_exec(handle, initSpatial, 0, 0, &errMsg);
            if (ret != SQLITE_OK) {
                printf("Error al inicializar SpatiaLite: %s\n", errMsg);
                sqlite3_free(errMsg);
                sqlite3_close(handle);
                return -1;
            }
            spatial_initialized = 1;
        }
    }

    sqlite3_finalize(stmt);

    /* Cerrar la conexión a la base de datos */
    sqlite3_close(handle);
    spatialite_cleanup_ex(cache);

    return spatial_initialized;
}

int execute_query(sqlite3 *handle, const char *query, char **errMsg, char ***columnNames, char ***rows, int *rowCount, int *colCount) {
    sqlite3_stmt *stmt;
    int ret = sqlite3_prepare_v2(handle, query, -1, &stmt, NULL);
    
    if (ret != SQLITE_OK) {
        *errMsg = strdup(sqlite3_errmsg(handle));
        return ret;
    }

    *rowCount = 0;
    *colCount = sqlite3_column_count(stmt);
    *columnNames = (char **)malloc(*colCount * sizeof(char *));
    
    // Obtener los nombres de las columnas
    for (int i = 0; i < *colCount; i++) {
        (*columnNames)[i] = strdup(sqlite3_column_name(stmt, i));
    }

    // Crear un buffer dinámico para las filas
    *rows = NULL;

    while (sqlite3_step(stmt) == SQLITE_ROW) {
        (*rowCount)++;
        *rows = (char **)realloc(*rows, (*rowCount) * (*colCount) * sizeof(char *));
        
        for (int i = 0; i < *colCount; i++) {
            if (sqlite3_column_type(stmt, i) == SQLITE_TEXT) {
                (*rows)[((*rowCount) - 1) * (*colCount) + i] = strdup((const char *)sqlite3_column_text(stmt, i));
            } else {
                (*rows)[((*rowCount) - 1) * (*colCount) + i] = strdup("");
            }
        }
    }

    sqlite3_finalize(stmt);
    return SQLITE_OK;
}


