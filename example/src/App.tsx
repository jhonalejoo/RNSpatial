import { useEffect } from 'react';
import { StyleSheet, View, Text, Button, Alert } from 'react-native';
import RNSpatial from 'react-native-fnc-spatialite';
var RNFS = require('react-native-fs');

export default function App() {
  const logFilePath = RNFS.DocumentDirectoryPath + '/dbSica.sqlite';
  console.log(logFilePath,'log')

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
  }, []); // Agregar 'logFilePath' como dependencia

  const execute =async ()=>{
    const result = await RNSpatial.executeQuery('SELECT  VER_PK from SC_VEREDAS WHERE  VER_PK =  10131');
    console.log(result)
  }

  return (
    <View style={styles.container}>
      <Text>Result: 2</Text>
      <Button
      onPress={()=> execute()}
      title={'ejemplo'}
      />
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
