import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  connect(paramsDataBase: { dbName: string; localPath?: string }): Promise<{
    isConnected: boolean;
    isSpatial: boolean;
  }>;
  close(): Promise<{ isConnected: boolean }>;
  executeQuery(query: string): Promise<{
    rows: number;
    cols: number;
    data: Array<Record<string, any>>;
  }>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RNSpatial');
