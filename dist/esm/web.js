import { WebPlugin } from '@capacitor/core';
export class BluetoothSerialWeb extends WebPlugin {
    async connect(options) {
        console.debug('Attempting to connect to', options.address);
        throw this.unimplemented('Not implemented on web.');
    }
    async disconnect() {
        throw this.unimplemented('Not implemented on web.');
    }
    async write(options) {
        console.debug('Attempting to write data:', options.data);
        throw this.unimplemented('Not implemented on web.');
    }
    async read() {
        throw this.unimplemented('Not implemented on web.');
    }
    async isEnabled() {
        throw this.unimplemented('Not implemented on web.');
    }
    async enable() {
        throw this.unimplemented('Not implemented on web.');
    }
    async scan() {
        throw this.unimplemented('Not implemented on web.');
    }
}
