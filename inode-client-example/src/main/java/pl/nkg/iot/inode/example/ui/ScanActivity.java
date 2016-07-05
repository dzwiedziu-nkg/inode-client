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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Set;

import pl.nkg.iot.inode.example.R;

public class ScanActivity extends AppCompatActivity
        implements ScanFragment.OnFragmentInteractionListener {

    public static final String BUNDLE_CHECKED = "checked";

    private BluetoothAdapter mBluetoothAdapter;
    private ScanFragment mScanFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScanFragment = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @Override
    protected void onDestroy() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        super.onDestroy();
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

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mScanFragment.addDevice(device, rssi, scanRecord);
                        }
                    });
                }
            };
}
