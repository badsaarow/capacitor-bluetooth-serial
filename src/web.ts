import { WebPlugin, PluginListenerHandle } from '@capacitor/core';
import {
  BluetoothConnectOptions,
  BluetoothConnectResult,
  BluetoothStopNotificationsOptions,
  BluetoothStartNotificationsOptions,
  BluetoothReadOptions,
  BluetoothReadResult,
  BluetoothReadUntilOptions,
  BluetoothScanResult,
  BluetoothSerialPlugin,
  BluetoothState,
  BluetoothWriteOptions
} from './definitions';
import {OptionsRequiredError} from './utils/errors';

export class BluetoothSerialWeb extends WebPlugin implements BluetoothSerialPlugin {
  async isEnabled(): Promise<BluetoothState> {
    // not available on web
    return {enabled: true};
  }

  async enable(): Promise<BluetoothState> {
    throw this.unavailable('enable is not available on web.');
  }

  disable(): Promise<BluetoothState> {
    throw this.unavailable('disable is not available on web.');
  }

  startEnabledNotifications(): Promise<void> {
    throw this.unavailable('disable is not available on web.');
  }

  stopEnabledNotifications(): Promise<void> {
    throw this.unavailable('disable is not available on web.');
  }

  async scan(): Promise<BluetoothScanResult> {
    throw new Error('Method not implemented.');
  }

  async connect(options: BluetoothConnectOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async connectInsecure(options: BluetoothConnectOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async disconnect(options: BluetoothConnectOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async isConnected(options: BluetoothConnectOptions): Promise<BluetoothConnectResult> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async read(options: BluetoothReadOptions):
      Promise<BluetoothReadResult> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async readUntil(options: BluetoothReadUntilOptions): Promise<BluetoothReadResult> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async write(options: BluetoothWriteOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async startNotifications(options: BluetoothStartNotificationsOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  async stopNotifications(options: BluetoothStopNotificationsOptions): Promise<void> {
    if (!options) {
      return Promise.reject(new OptionsRequiredError());
    }
    throw new Error('Method not implemented.');
  }

  addListener(eventName: string, listenerFunc: (...args: any[]) => any): Promise<PluginListenerHandle> & PluginListenerHandle {
    const pluginListenerHandle = super.addListener(eventName, listenerFunc);
  
    // Create a new PluginListenerHandle that uses the remove method from the PluginListenerHandle returned by super.addListener
    const newPluginListenerHandle: PluginListenerHandle = {
      remove: async () => {
        const resolvedPluginListenerHandle = await pluginListenerHandle;
        await resolvedPluginListenerHandle.remove();
      },
    };
  
    // Return a Promise that resolves to the new PluginListenerHandle, and also has the properties of the new PluginListenerHandle
    return Object.assign(Promise.resolve(newPluginListenerHandle), newPluginListenerHandle);
  }
}

const BluetoothSerial = new BluetoothSerialWeb();

export { BluetoothSerial };
