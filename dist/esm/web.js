import { WebPlugin } from '@capacitor/core';
import { OptionsRequiredError } from './utils/errors';
export class BluetoothSerialWeb extends WebPlugin {
    async isEnabled() {
        // not available on web
        return { enabled: true };
    }
    async enable() {
        throw this.unavailable('enable is not available on web.');
    }
    disable() {
        throw this.unavailable('disable is not available on web.');
    }
    startEnabledNotifications() {
        throw this.unavailable('disable is not available on web.');
    }
    stopEnabledNotifications() {
        throw this.unavailable('disable is not available on web.');
    }
    async scan() {
        throw new Error('Method not implemented.');
    }
    async connect(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async connectInsecure(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async disconnect(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async isConnected(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async read(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async readUntil(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async write(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async startNotifications(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    async stopNotifications(options) {
        if (!options) {
            return Promise.reject(new OptionsRequiredError());
        }
        throw new Error('Method not implemented.');
    }
    addListener(eventName, listenerFunc) {
        const pluginListenerHandle = super.addListener(eventName, listenerFunc);
        // Create a new PluginListenerHandle that uses the remove method from the PluginListenerHandle returned by super.addListener
        const newPluginListenerHandle = {
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
//# sourceMappingURL=web.js.map