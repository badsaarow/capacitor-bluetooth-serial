package com.bluetoothserial;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;

import com.bluetoothserial.plugin.BluetoothSerialPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class BluetoothSerialService {
    private static final UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "BluetoothSerialService";

    private BluetoothAdapter adapter;
    private BluetoothSerialPlugin plugin;
    private Map<String, BluetoothConnection> connections = new HashMap<>();

    public BluetoothSerialService(BluetoothSerialPlugin plugin, BluetoothAdapter adapter) {
        this.plugin = plugin;
        this.adapter = adapter;
    }

    public void connect(BluetoothDevice device, BluetoothSerialPlugin serial) {
        connect(device, true, serial);
    }

    public void connectInsecure(BluetoothDevice device, BluetoothSerialPlugin serial) {
        connect(device, false, serial);
    }

    private void connect(BluetoothDevice device, boolean secure, BluetoothSerialPlugin serial) {
        BluetoothConnection connection = new BluetoothConnection(device, secure, serial);
        connection.start();

        connections.put(device.getAddress(), connection);
    }

    public boolean disconnectAllDevices() {
        boolean success = true;
        for(String address : connections.keySet()) {
            success = success & disconnect(address);
        }

        return success;
    }

    public boolean disconnect(BluetoothDevice device) {
        String address = device.getAddress();
        return disconnect(address);
    }

    public boolean disconnect(String address) {
        Log.d(TAG, "BEGIN disconnect device " + address);

        BluetoothConnection socket = getConnection(address);

        if(socket == null) {
            Log.e(TAG, "No connection found");
            return true;
        }

        if(!socket.isConnected()) {
            Log.i(TAG, "Device is already disconnected");
        } else {
            return socket.disconnect();
        }

        BluetoothConnection connection = connections.get(address);
        if(connection != null) {
            connection.interrupt();
        }

        connections.remove(address);
        Log.d(TAG, "END disconnect device " + address);

        return true;
    }

    public boolean isConnected(String address) {
        Log.d(TAG, "BEGIN isConnected device " + address);

        BluetoothConnection socket = getConnection(address);

        if(socket == null) {
            Log.e(TAG, "No connection found");
            return false;
        }

        return socket.isConnected();
    }

    /**
     * Write to the connected Device via socket.
     *
     * @param address The device address to send
     * @param out  The bytes to write
     */
    public boolean write(String address, byte[] out) {
        Log.d(TAG, "BEGIN write to device " + address + " with " + out.length + " bytes");
        Log.d(TAG, "Data to write: " + bytesToHex(out));

        BluetoothConnection r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            r = getConnection(address);
        }

        if(r == null || !r.isConnected()) {
            Log.e(TAG, "Write failed - No connection or not connected");
            return false;
        }

        // Perform the write unsynchronized
        r.write(out);
        Log.d(TAG, "END write to device " + address);

        return true;
    }

    public String read(String address) throws IOException {
        BluetoothConnection connection = getConnection(address);

        if(connection == null) {
            Log.e(TAG, "No connection found");
            throw new IOException("No connection found");
        }

        if(!connection.isConnected()) {
            Log.e(TAG, "Not connected");

            throw new IOException("Not connected");
        }

        return connection.read();
    }

    public String readUntil(String address, String delimiter) throws IOException {
        BluetoothConnection connection = getConnection(address);

        if(connection == null) {
            Log.e(TAG, "No connection found");
            throw new IOException("No connection found");
        }

        if(!connection.isConnected()) {
            Log.e(TAG, "Not connected");

            throw new IOException("Not connected");
        }

        return connection.readUntil(delimiter);
    }

    public void startNotifications(String address, String delimiter, Consumer<String> callback) throws IOException {
        BluetoothConnection connection = getConnection(address);

        if (connection == null) {
            Log.e(TAG, "No connection found");
            throw new IOException("No connection found");
        }

        if (!connection.isConnected()) {
            Log.e(TAG, "Not connected");
            throw new IOException("Not connected");
        }

        connection.startNotifications(delimiter, callback);
    }

    public void stopNotifications(String address) throws IOException {
        BluetoothConnection connection = getConnection(address);

        if(connection == null) {
            Log.e(TAG, "No connection found");
            throw new IOException("No connection found");
        }

        if(!connection.isConnected()) {
            Log.e(TAG, "Not connected");

            throw new IOException("Not connected");
        }

        connection.stopNotifications();
    }

    private BluetoothConnection getConnection(String address) {
        return connections.get(address);
    }

    public void stopAll() {
        disconnectAllDevices();
    }

    public void stop(String address) {
        disconnect(address);
    }

    public void reconnectAll() {
        List<String> addresses = new ArrayList<>(connections.keySet());

        for(String address : addresses) {
            reconnect(address);
        }
    }

    public void reconnect(String address) {
        BluetoothConnection oldConnection = connections.get(address);
        BluetoothConnection newConnection = new BluetoothConnection(oldConnection);
        disconnect(address);
        newConnection.start();
        connections.put(address, newConnection);
    }

    private enum ConnectionStatus {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED;
    }

    private class BluetoothConnection extends Thread {
        private final BluetoothDevice device;
        private final boolean secure;
        private final BluetoothSerialPlugin plugin;

        private BluetoothSocket socket = null;
        private InputStream socketInputStream;
        private OutputStream socketOutputStream;

        private boolean enabledNotifications;
        private String notificationDelimiter;
        private final StringBuffer readBuffer;
        private Consumer<String> notificationCallback;
        private ConnectionStatus status;
        private volatile boolean running = true;

        @SuppressLint("MissingPermission")
        public BluetoothConnection(BluetoothDevice device, boolean secure, BluetoothSerialPlugin plugin) {
            this.device = device;
            this.secure = secure;
            this.plugin = plugin;
            this.status = ConnectionStatus.NOT_CONNECTED;
            adapter.cancelDiscovery();
            readBuffer = new StringBuffer();
            this.enabledNotifications = false;

            createRfcomm(device, secure);
        }

        public BluetoothConnection(BluetoothConnection connection) {
            this(connection.device, connection.secure, connection.plugin);
            this.enabledNotifications = connection.enabledNotifications;
            this.notificationCallback = connection.notificationCallback;
            this.notificationDelimiter = connection.notificationDelimiter;
        }

        @SuppressLint("MissingPermission")
        private void createRfcomm(BluetoothDevice device, boolean secure) {
            String socketType = secure ? "Secure" : "Insecure";
            Log.d(TAG, "BEGIN create socket SocketType:" + socketType + " for device " + device.getAddress());
            status = ConnectionStatus.CONNECTING;
            try {
                if(secure) {
                    socket = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
                } else {
                    socket = device.createInsecureRfcommSocketToServiceRecord(DEFAULT_UUID);
                }

                Log.d(TAG, "Socket created successfully for device " + device.getAddress());
                Log.d(TAG, "BEGIN connect SocketType:" + socketType);

                socket.connect();

                Log.i(TAG, "Connection success - SocketType:" + socketType);
                Log.d(TAG, "END connect SocketType:" + socketType);

                // Get streams immediately after successful connection
                socketInputStream = socket.getInputStream();
                socketOutputStream = socket.getOutputStream();
                
                if (socketInputStream == null || socketOutputStream == null) {
                    throw new IOException("Failed to get socket streams");
                }

                // Set socket timeout to prevent blocking forever
//                socket.setSoTimeout(1000);
                
                Log.d(TAG, "Successfully obtained input/output streams");
                connected();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + socketType + " create() failed for device " + device.getAddress(), e);
                connectionFailed();
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the socket during failure", closeException);
                }
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN connectedThread for device " + device.getAddress());
            byte[] bytesBuffer = new byte[1024];
            int retryCount = 0;
            final int MAX_RETRIES = 3;

            // Keep listening to the InputStream while connected
            while (running) {
                if (status == ConnectionStatus.CONNECTED) {
                    try {
                        // Check if input stream is available
                        if (socketInputStream == null) {
                            Log.e(TAG, "InputStream is null for device " + device.getAddress());
                            break;
                        }
                        
                        // Check available bytes
                        int available = socketInputStream.available();
                        if (available > 0) {
                            retryCount = 0; // Reset retry count on successful read
                            Log.d(TAG, available + " bytes available to read from " + device.getAddress());
                            
                            int length = socketInputStream.read(bytesBuffer, 0, Math.min(available, bytesBuffer.length));
                            if (length == -1) {
                                Log.e(TAG, "End of stream reached for device " + device.getAddress());
                                break;
                            }
                            
                            if (length > 0) {
                                String data = new String(bytesBuffer, 0, length);
                                Log.d(TAG, "Received " + length + " bytes from " + device.getAddress());
                                Log.d(TAG, "Raw data (hex): " + bytesToHex(bytesBuffer, 0, length));
                                Log.d(TAG, "Data as string: " + data);
                                updateBuffer(data);
                            }
                        } else {
                            retryCount++;
                            if (retryCount > MAX_RETRIES) {
                                // Check if socket is still valid
                                if (!isConnected()) {
                                    Log.e(TAG, "Socket disconnected for device " + device.getAddress());
                                    break;
                                }
                                retryCount = 0; // Reset retry count if socket is still connected
                            }
                        }
                        
                        // Small sleep to prevent CPU thrashing
                        Thread.sleep(50); // Increased sleep time when no data
                        
                    } catch (IOException e) {
                        if (e.getMessage() != null && e.getMessage().contains("socket closed")) {
                            Log.d(TAG, "Socket closed normally for device " + device.getAddress());
                        } else {
                            Log.e(TAG, "Device " + device.getAddress() + " disconnected with error: " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                        disconnect();
                        break;
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Read thread interrupted for device " + device.getAddress());
                        break;
                    } catch (Exception e) {
                        Log.e(TAG, "Unexpected error while reading from device " + device.getAddress(), e);
                        e.printStackTrace();
                        if (++retryCount > MAX_RETRIES) {
                            Log.e(TAG, "Too many errors, disconnecting from device " + device.getAddress());
                            disconnect();
                            break;
                        }
                        continue;
                    }
                } else {
                    Log.d(TAG, "Connection status for " + device.getAddress() + ": " + status);
                    try {
                        // Sleep briefly to avoid busy waiting
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Read thread interrupted for device " + device.getAddress());
                        break;
                    }
                }
            }
            Log.i(TAG, "END connectedThread for device " + device.getAddress());
        }

        private void updateBuffer(String data) {
            if (data == null || data.isEmpty()) {
                Log.d(TAG, "Received empty data for device " + device.getAddress());
                return;
            }

            synchronized (this.readBuffer) {
                Log.d(TAG, "Updating buffer for " + device.getAddress());
                Log.d(TAG, "Current buffer size: " + readBuffer.length());
                this.readBuffer.append(data);
                Log.d(TAG, "New buffer size: " + readBuffer.length());
                Log.d(TAG, "Buffer content: " + readBuffer.toString());
            }
            
            if (this.enabledNotifications && this.notificationCallback != null) {
                Log.d(TAG, "Sending notification for " + device.getAddress());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    try {
                        notificationCallback.accept(data);
                        Log.d(TAG, "Notification sent successfully with data length: " + data.length());
                        Log.d(TAG, "Notification data: " + data);
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending notification: " + e.getMessage(), e);
                    }
                }
            } else {
                Log.d(TAG, "Notifications not enabled or callback is null for device " + device.getAddress());
            }
        }

        public synchronized String read() {
            String data;
            synchronized (readBuffer) {
                int index = readBuffer.length();

                data = readBuffer.substring(0, index);
                readBuffer.delete(0, index);
            }

            return data;
        }

        public synchronized String readUntil(String delimiter) {
            String data = "";
            synchronized (readBuffer) {
                int index = readBuffer.indexOf(delimiter);

                if (index >= 0) {
                    index += delimiter.length();
                    data = readBuffer.substring(0, index);
                    readBuffer.delete(0, index+1);
                }
            }

            return data;
        }

        public synchronized void startNotifications(String delimiter, Consumer<String> callback) {
            Log.d(TAG, "Starting notifications for device " + device.getAddress());
            enabledNotifications = true;
            this.notificationDelimiter = delimiter;
            this.notificationCallback = callback;
            Log.d(TAG, "Notifications started with delimiter: " + delimiter);
        }

        public synchronized void stopNotifications() {
            Log.d(TAG, "Stopping notifications for device " + device.getAddress());
            enabledNotifications = false;
            this.notificationCallback = null;
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                Log.d(TAG, "Writing " + buffer.length + " bytes to " + device.getAddress());
                Log.d(TAG, "Data to write (hex): " + bytesToHex(buffer));
                socketOutputStream.write(buffer);
                Log.d(TAG, "Write completed successfully");
            } catch (IOException e) {
                Log.e(TAG, "Exception during write to " + device.getAddress(), e);
            }
        }

        public boolean disconnect() {
            Log.d(TAG, "BEGIN disconnect for device " + device.getAddress());
            running = false;
            try {
                socket.close();
                Log.d(TAG, "Socket closed successfully");
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed for " + device.getAddress(), e);
                return false;
            }
            status = ConnectionStatus.NOT_CONNECTED;
            Log.d(TAG, "END disconnect for device " + device.getAddress());
            return true;
        }

        public void reconnect() {
            try {
                socket.close();
            } catch (IOException io) {
                Log.e(TAG, "Error closing connection", io);
            }

            createRfcomm(device, secure);
            socketInputStream = getInputStream(socket);
            socketOutputStream = getOutputStream(socket);

        }

        private void connected() {
            Log.d(TAG, "BEGIN connected for device " + device.getAddress());
            status = ConnectionStatus.CONNECTED;
            this.plugin.connected();
            Log.d(TAG, "END connected for device " + device.getAddress());
        }

        private void connectionFailed() {
            Log.e(TAG, "Connection Failed for device " + device.getAddress());
            Log.d(TAG, "Connection status before failure: " + status);
            status = ConnectionStatus.NOT_CONNECTED;
            this.plugin.connectionFailed();
            Log.d(TAG, "Connection status after failure: " + status);
        }

        public boolean isConnected() {
            return socket.isConnected();
        }

        private InputStream getInputStream(BluetoothSocket socket) {
            try {
                return socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while getting inputStream", e);
            }

            return null;
        }

        private OutputStream getOutputStream(BluetoothSocket socket) {
            try {
                return socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error while getting outputStream", e);
            }

            return null;
        }
    }

    // Utility method to convert bytes to hex string
    private static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, 0, bytes.length);
    }

    private static String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }
}
