declare module "@capacitor/core" {
  interface PluginRegistry {
    BluetoothSerial: BluetoothSerialPlugin;
  }
}

export interface BluetoothSerialPlugin {
  isEnabled(): Promise<BluetoothEnabledResult>;
  scan(): Promise<BluetoothScanResult>;

  echo(options: { value: string }): Promise<{value: string}>;
}

export interface BluetoothEnabledResult {
  enabled: boolean;
}

export interface BluetoothScanResult {
  devices: BluetoothDevice[];
}

export interface BluetoothDevice {
  name: string;
  id: string;
  address: string;
  class: number;
  uuid: string;
  rssi: number;
}