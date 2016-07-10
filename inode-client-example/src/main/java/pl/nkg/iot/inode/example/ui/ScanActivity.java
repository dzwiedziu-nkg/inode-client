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

import java.util.Set;

import pl.nkg.iot.inode.android.BleScannerEngine;
import pl.nkg.iot.inode.example.R;
import pl.nkg.iot.inode.example.events.BleScanDetectedEvent;
import pl.nkg.iot.inode.example.services.BleScannerService;

public class ScanActivity extends AppCompatActivity
        implements ScanFragment.OnFragmentInteractionListener {

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
    }
}
