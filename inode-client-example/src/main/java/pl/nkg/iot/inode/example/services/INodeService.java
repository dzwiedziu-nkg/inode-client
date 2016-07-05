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

package pl.nkg.iot.inode.example.services;

import org.greenrobot.eventbus.EventBus;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.nkg.iot.inode.android.BleCommunicationProvider;
import pl.nkg.iot.inode.core.DecodedRecord;
import pl.nkg.iot.inode.core.DownloadManager;
import pl.nkg.iot.inode.core.DownloadManagerListener;
import pl.nkg.iot.inode.core.LogProvider;
import pl.nkg.iot.inode.example.events.LogEvent;

public class INodeService extends Service implements DownloadManagerListener, LogProvider {
    private final static String TAG = INodeService.class.getSimpleName();

    private String mBluetoothDeviceAddress;

    private DownloadManager mDownloadManager;
    private BleCommunicationProvider mCommunicationProvider;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.US);

    @Override
    public void println(int priority, String tag, String content, Throwable tr) {
        Log.println(priority, tag, content);
        EventBus.getDefault().post(new LogEvent(priority, tag, content, tr));
    }

    public class LocalBinder extends Binder {
        public INodeService getService() {
            return INodeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        mCommunicationProvider = new BleCommunicationProvider(this);
        mDownloadManager = new DownloadManager(mCommunicationProvider, this, this);
        mCommunicationProvider.setCommunicationEventListener(mDownloadManager);

        return mCommunicationProvider.initialize(this);
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
    public boolean connect(final String address) {
        mBluetoothDeviceAddress = address;
        return mCommunicationProvider.connect(this, address);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int,
     * int)}
     * callback.
     */
    public void disconnect() {
        mCommunicationProvider.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        mCommunicationProvider.close();
    }

    @Override
    public void onChangeState() {
    }

    @Override
    public void onDataAvailable() {
        StringBuilder sb = new StringBuilder();

        for (DecodedRecord decodedRecord : mDownloadManager.getDecodedRecords()) {
            String msg = dateFormat.format(new Date(decodedRecord.getTimestamp()))
                    + ", "
                    + decodedRecord.getValue()
                    + " "
                    + decodedRecord.getType();
            Log.v(TAG, msg);
            EventBus.getDefault().post(new LogEvent(LogProvider.VERBOSE, TAG, msg, null));

            sb.append(mBluetoothDeviceAddress)
                    .append("|")
                    .append(dateFormat.format(new Date(decodedRecord.getTimestamp())))
                    .append("|")
                    .append(decodedRecord.getValue())
                    .append("|")
                    .append(decodedRecord.getType())
                    .append("\n");
        }
        PostRestService.startService(this, sb.toString());
    }
}
