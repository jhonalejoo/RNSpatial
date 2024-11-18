import { useEffect } from 'react';
import { StyleSheet, View, Text, Button, Alert, Platform } from 'react-native';
import RNSpatial from 'react-native-fnc-spatialite';
var RNFS = require('react-native-fs');

export default function App() {
  const logFilePath = RNFS.DocumentDirectoryPath + '/dbSica.sqlite';

  useEffect(() => {
    const initializeDatabase = async () => {
      try {
        const result = await RNSpatial.connect({ dbName: Platform.OS === 'android' ? 'dbSica.sqlite'  : logFilePath});
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
  const exec =()=> {
    return new Promise((resolve, reject) => {
        let validation = false;
        if (validation === false) {
          RNSpatial.executeQuery('SELECT  VER_PK from SC_VEREDAS WHERE  VER_PK =  10131').then(response => {
                console.log(response,'response')
                const result = [];
                for (let i = 0; i < response.data.length; ++i) {
                    const objResult = {};
                    for (var key in response.data[i]) {
                        objResult[key.toUpperCase()] = response.data[i][key];
                    }
                    result.push(objResult);
                }
                console.log(result)
                resolve(result);
            }).catch(error => resolve({ result: error }));
        } else {
            resolve(undefined);
        }
    });
}

  return (
    <View style={styles.container}>
      <Text>Result: 2</Text>
      <Button
      onPress={()=> exec()}
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
