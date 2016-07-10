/*
 * Copyright (c) by Michał Niedźwiecki 2016
 * Contact: nkg753 on gmail or via GitHub profile: dzwiedziu-nkg
 *
 * This file is part of inode-client.
 *
 * inode-client is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * inode-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package pl.nkg.iot.inode.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

import pl.nkg.iot.inode.core.ByteUtils;
import pl.nkg.iot.inode.core.CommunicationEventListener;
import pl.nkg.iot.inode.core.CommunicationEventType;
import pl.nkg.iot.inode.core.CommunicationProvider;
import pl.nkg.iot.inode.core.LogProvider;

public class BleCommunicationProvider implements CommunicationProvider {

    private final static String TAG = BleCommunicationProvider.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private CommunicationEventListener mListener;
    private LogProvider mLogProvider;
    private boolean mDiscovered = false;
    private boolean mDoLogin = true;

    public BleCommunicationProvider(LogProvider logProvider) {
        mLogProvider = logProvider;
    }

    public void setCommunicationEventListener(CommunicationEventListener listener) {
        mListener = listener;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                mListener.onCommunicationEvent(CommunicationEventType.disconnected, null, null, null, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mDiscovered = true;
                runIfCan();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logData("onCharacteristicRead", -1, characteristic.getValue(), false, 0, characteristic.getUuid());
                mListener.onCommunicationEvent(CommunicationEventType.characteristicRead, characteristic.getValue(), characteristic.getService().getUuid(), characteristic.getUuid(), null);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //logData("onCharacteristicChanged", -1, characteristic.getValue(), false, 0, characteristic.getUuid());
            mListener.onCommunicationEvent(CommunicationEventType.characteristicChanged, characteristic.getValue(), characteristic.getService().getUuid(), characteristic.getUuid(), null);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logData("onCharacteristicWrite", 1, characteristic.getValue(), false, 0, characteristic.getUuid());
                mListener.onCommunicationEvent(CommunicationEventType.characteristicWrite, characteristic.getValue(), characteristic.getService().getUuid(), characteristic.getUuid(), null);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logData("onDescriptorWrite", 1, descriptor.getValue(), false, 0, descriptor.getUuid());
                mListener.onCommunicationEvent(CommunicationEventType.descriptorWrite, descriptor.getValue(), descriptor.getCharacteristic().getService().getUuid(), descriptor.getCharacteristic().getUuid(), descriptor.getUuid());
            }
        }

    };

    private void logData(String eventName, int direction, byte[] data, boolean wrap8bytes, int offset, UUID uuid) {
        if (!mDoLogin) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(eventName).append("(").append(uuid.toString()).append(")");
        switch (direction) {
            case -1:
                sb.append(" <-- ");
                break;

            case 1:
                sb.append(" --> ");
                break;
        }

        String prefix = sb.toString();
        sb = new StringBuilder();
        sb.append(prefix).append("\n");

        sb.append(ByteUtils.formatBytes(data, wrap8bytes, offset));

        mLogProvider.println(LogProvider.VERBOSE, TAG, sb.toString(), null);
        //Log.d(TAG, sb.toString());
    }

    private void runIfCan() {
        if (mDiscovered && mConnectionState == STATE_CONNECTED) {
            mListener.enableToRun();
        }
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(Context context) {
        // TODO: move to constructor and throw exception when failed
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    public boolean connect(Context context, final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @Override
    public void writeToDescriptor(UUID service, UUID characteristic, UUID descriptor, byte[] data) {
        BluetoothGattDescriptor desc = mBluetoothGatt.getService(service).getCharacteristic(characteristic).getDescriptor(descriptor);
        desc.setValue(data);
        mBluetoothGatt.writeDescriptor(desc);
    }

    @Override
    public void writeToCharacteristic(UUID service, UUID characteristic, byte[] data) {
        BluetoothGattCharacteristic chr = mBluetoothGatt.getService(service).getCharacteristic(characteristic);
        chr.setValue(data);
        mBluetoothGatt.writeCharacteristic(chr);
    }

    @Override
    public void requestReadFromDescriptor(UUID service, UUID characteristic, UUID descriptor) {
        BluetoothGattDescriptor desc = mBluetoothGatt.getService(service).getCharacteristic(characteristic).getDescriptor(descriptor);
        mBluetoothGatt.readDescriptor(desc);
    }

    @Override
    public void requestReadFromCharacteristic(UUID service, UUID characteristic) {
        BluetoothGattCharacteristic chr = mBluetoothGatt.getService(service).getCharacteristic(characteristic);
        mBluetoothGatt.readCharacteristic(chr);
    }

    @Override
    public void requestNotificationFromCharacteristic(UUID service, UUID characteristic) {
        // TODO: timeout watchdog
    }

    @Override
    public void setCharacteristicNotification(UUID service, UUID characteristic, boolean enable) {
        BluetoothGattCharacteristic chr = mBluetoothGatt.getService(service).getCharacteristic(characteristic);
        mBluetoothGatt.setCharacteristicNotification(chr, enable);
    }
}
