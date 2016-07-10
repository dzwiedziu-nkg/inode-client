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

package pl.nkg.iot.inode.example.ui;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import pl.nkg.iot.inode.android.BleScannerEngine;
import pl.nkg.iot.inode.core.ByteUtils;
import pl.nkg.iot.inode.core.DecodedRecord;
import pl.nkg.iot.inode.core.RecordType;
import pl.nkg.iot.inode.core.ValuesDecoder;
import pl.nkg.iot.inode.example.R;
import pl.nkg.iot.inode.example.events.BleScanDetectedEvent;
import pl.nkg.iot.inode.example.services.BleScannerService;
import pl.nkg.iot.inode.example.services.INodeService;

public class ScanActivity extends AppCompatActivity
        implements ScanFragment.OnFragmentInteractionListener {

    private static final String TAG = ScanActivity.class.getSimpleName();
    public static final String BUNDLE_CHECKED = "checked";

    private ScanFragment mScanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScanFragment = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        BleScannerService.startService(this, BleScannerEngine.SCAN_MODE_LOW_LATENCY);
    }

    @Override
    protected void onStop() {
        BleScannerService.stopService(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        Set<BluetoothDevice> checkedDevices = mScanFragment.getChecked();
        String[] checkedAddress = new String[checkedDevices.size()];
        int i = 0;
        for (BluetoothDevice bd : checkedDevices) {
            checkedAddress[i] = bd.getAddress();
            i++;
        }
        data.putExtra(BUNDLE_CHECKED, checkedAddress);
        setResult(1, data);
        super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BleScanDetectedEvent event) {
        mScanFragment.addDevice(event.mBluetoothDevice, event.mRssi, event.mData);
        byte[] data = event.mData;
        List<DecodedRecord> valuesList = new ArrayList<>();
        if (data != null) {

            switch (data[6]) {
                case (byte) 0x9D:
                    // PHT
                    valuesList.add(new DecodedRecord(
                            System.currentTimeMillis(),
                            RecordType.temperature,
                            ValuesDecoder.decode(RecordType.temperature,
                                    ByteUtils.extractLEU16(data, 13))));

                    valuesList.add(new DecodedRecord(
                            System.currentTimeMillis(),
                            RecordType.pressure,
                            ValuesDecoder.decode(RecordType.pressure,
                                    ByteUtils.extractLEU16(data, 11))));

                    valuesList.add(new DecodedRecord(
                            System.currentTimeMillis(),
                            RecordType.humidity,
                            ValuesDecoder.decode(RecordType.humidity,
                                    ByteUtils.extractLEU16(data, 15))));
                    break;

                case (byte) 0x89:
                    Log.d(TAG, event.mBluetoothDevice.getAddress() + ": "
                            + ByteUtils.formatBytes(event.mData, false, 0));
                    // NAV
                    break;
            }
        }

        for (DecodedRecord decodedRecord : valuesList) {
            String msg = INodeService.dateFormat.format(new Date(decodedRecord.getTimestamp()))
                    + ", "
                    + decodedRecord.getValue()
                    + " "
                    + decodedRecord.getType();
            Log.v(TAG, msg);
        }
    }
}
