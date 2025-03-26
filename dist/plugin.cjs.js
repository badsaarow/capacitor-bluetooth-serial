'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var core = require('@capacitor/core');

const BluetoothSerial = core.registerPlugin('BluetoothSerial', {
    web: () => Promise.resolve().then(function () { return web; }).then(m => new m.BluetoothSerialWeb()),
});

class BluetoothSerialWeb extends core.WebPlugin {
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

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    BluetoothSerialWeb: BluetoothSerialWeb
});

exports.BluetoothSerial = BluetoothSerial;
//# sourceMappingURL=plugin.cjs.js.map
