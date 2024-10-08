#ifndef RNSpatial_h
#define RNSpatial_h

#include <sqlite3.h>  

int myopen(const char *filename);

// Declaración de la nueva función execute_query
int execute_query(sqlite3 *handle, const char *query, char **errMsg, char ***columnNames, char ***rows, int *rowCount, int *colCount);

#endif /* RNSpatial_h */
