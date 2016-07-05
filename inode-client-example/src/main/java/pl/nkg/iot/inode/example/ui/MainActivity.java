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

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

import pl.nkg.iot.inode.example.MyApplication;
import pl.nkg.iot.inode.example.R;

public class MainActivity extends AppCompatActivity
        implements DevicesFragment.OnFragmentInteractionListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSION_RESPONSE = 2;
    private static final int SCAN_IOT_DEVICES_RESPONSE = 3;

    private BluetoothAdapter mBluetoothAdapter;
    private MyApplication mApplication;
    private DevicesFragment mDevicesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApplication = (MyApplication) getApplication();
        mDevicesFragment = (DevicesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        // Prompt for permissions
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("BleActivity", "Location access not granted!");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSION_RESPONSE);
            }
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_scan:
                startActivityForResult(new Intent(this, ScanActivity.class), SCAN_IOT_DEVICES_RESPONSE);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCAN_IOT_DEVICES_RESPONSE:
                Set<String> nodes = mApplication.getPreferencesProvider().getPrefNodes();
                nodes.addAll(Arrays.asList(data.getStringArrayExtra(ScanActivity.BUNDLE_CHECKED)));
                mApplication.getPreferencesProvider().setPrefNodes(nodes);
                mDevicesFragment.refreshList();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNodeRemoved(String value) {
        Set<String> nodes = mApplication.getPreferencesProvider().getPrefNodes();
        nodes.remove(value);
        mApplication.getPreferencesProvider().setPrefNodes(nodes);
        mDevicesFragment.refreshList();
        Toast.makeText(this, getResources().getString(R.string.toast_device_removed, value), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNodeClick(String value) {
        DeviceActivity.startActivity(this, value);
    }
}
