import { WebPlugin } from '@capacitor/core';

import type { BluetoothSerialPlugin } from './definitions';

export class BluetoothSerialWeb
  extends WebPlugin
  implements BluetoothSerialPlugin
{
  async connect(options: { address: string }): Promise<void> {
    console.debug('Attempting to connect to', options.address);
    throw this.unimplemented('Not implemented on web.');
  }

  async disconnect(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async write(options: { data: string }): Promise<void> {
    console.debug('Attempting to write data:', options.data);
    throw this.unimplemented('Not implemented on web.');
  }

  async read(): Promise<{ data: string }> {
    throw this.unimplemented('Not implemented on web.');
  }

  async isEnabled(): Promise<{ enabled: boolean }> {
    throw this.unimplemented('Not implemented on web.');
  }

  async enable(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async scan(): Promise<{ devices: { name: string; address: string }[] }> {
    throw this.unimplemented('Not implemented on web.');
  }
}
