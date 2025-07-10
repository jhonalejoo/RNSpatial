import { useEffect } from 'react';
import { StyleSheet, View, Text, Button, Platform } from 'react-native';
import RNSpatial from 'react-native-fnc-spatialite';
let RNFS = require('react-native-fs');

export default function App() {
  const logFilePath = RNFS.DocumentDirectoryPath + '/dbSica.sqlite';

  useEffect(() => {
    const initializeDatabase = async () => {
      try {
        const result = await RNSpatial.connect({ dbName: logFilePath});
        console.log('Success:', result);
      } catch (error) {
        console.error('Error:', error);
      }
    };

    initializeDatabase(); // Iniciar el proceso de verificación y conexión
  }, []); // Agregar 'logFilePath' como dependencia

  const exec =()=> {
    return new Promise((resolve, reject) => {
        let validation = false;
        if (validation === false) {
          RNSpatial.executeQuery('SELECT  VER_PK from SC_VEREDAS WHERE  VER_PK =  10131').then(response => {
                console.log(response,'response')
                const result: Array<{ [key: string]: any }> = [];
                for (const element of response.data) {
                    const objResult: { [key: string]: any } = {};
                    for (let key in element) {
                        objResult[key.toUpperCase()] = element[key];
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
