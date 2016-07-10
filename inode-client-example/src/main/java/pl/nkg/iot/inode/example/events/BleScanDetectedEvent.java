package pl.nkg.iot.inode.example.events;

import android.bluetooth.BluetoothDevice;

public class BleScanDetectedEvent {
    final public BluetoothDevice mBluetoothDevice;
    final public int mRssi;
    final public byte[] mData;

    public BleScanDetectedEvent(BluetoothDevice bluetoothDevice, int rssi, byte[] data) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        mData = data;
    }
}
