import { useEffect } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import RNSpatial from 'react-native-fnc-spatialite';
var RNFS = require('react-native-fs');

export default function App() {
  const logFilePath = RNFS.DocumentDirectoryPath + '/dbSica.sqlite';

  useEffect(() => {
    const initializeDatabase = async () => {
      try {
        const result = await RNSpatial.connect({ dbName: logFilePath });
        console.log('Success:', result);
      } catch (error) {
        console.error('Error:', error);
      }
    };

    initializeDatabase(); // Iniciar el proceso de verificación y conexión
  }, [logFilePath]); // Agregar 'logFilePath' como dependencia

  return (
    <View style={styles.container}>
      <Text>Result: 2</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
