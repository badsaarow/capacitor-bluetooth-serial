export interface BluetoothSerialPlugin {
    connect(options: {
        address: string;
    }): Promise<void>;
    disconnect(): Promise<void>;
    write(options: {
        data: string;
    }): Promise<void>;
    read(): Promise<{
        data: string;
    }>;
    isEnabled(): Promise<{
        enabled: boolean;
    }>;
    enable(): Promise<void>;
    scan(): Promise<{
        devices: {
            name: string;
            address: string;
        }[];
    }>;
}
