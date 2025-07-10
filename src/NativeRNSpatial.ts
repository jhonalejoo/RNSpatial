import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface DatabaseParams {
  dbName: string;
  localPath?: string;
  readonly?: boolean;
  spatial?: boolean;
}

export interface ConnectionResult {
  isConnected: boolean;
  isSpatial?: boolean;
}

export interface QueryResult {
  rows: number;
  cols: number;
  data: any[];
}

export interface Spec extends TurboModule {
  connect(params: DatabaseParams): Promise<ConnectionResult>;
  close(): Promise<ConnectionResult>;
  executeQuery(query: string): Promise<QueryResult>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RNSpatial');
