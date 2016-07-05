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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import pl.nkg.iot.inode.example.R;
import pl.nkg.iot.inode.example.events.LogEvent;
import pl.nkg.iot.inode.example.services.INodeService;

public class DeviceActivity extends AppCompatActivity implements DeviceFragment.OnFragmentInteractionListener {

    private static final String TAG = DeviceActivity.class.getSimpleName();

    private static final String BUNDLE_ADDRESS = "device";
    private String mDeviceAddress;
    private INodeService mINodeService;
    private DeviceFragment mDeviceFragment;

    public static void startActivity(Context context, String deviceAddress) {
        Intent intent = new Intent(context, DeviceActivity.class);
        intent.putExtra(BUNDLE_ADDRESS, deviceAddress);
        context.startActivity(intent);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mINodeService = ((INodeService.LocalBinder) service).getService();
            if (!mINodeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mINodeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mINodeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        mDeviceAddress = getIntent().getStringExtra(BUNDLE_ADDRESS);
        mDeviceFragment = (DeviceFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        EventBus.getDefault().register(this);

        Intent gattServiceIntent = new Intent(this, INodeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LogEvent logEvent) {
        mDeviceFragment.println(logEvent.priority, logEvent.tag, logEvent.message, logEvent.throwable);
    }
}
