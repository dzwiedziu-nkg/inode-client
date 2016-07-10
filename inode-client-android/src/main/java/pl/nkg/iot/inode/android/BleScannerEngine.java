package pl.nkg.iot.inode.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public abstract class BleScannerEngine {

    public static final int SCAN_MODE_BALANCED = 1;
    public static final int SCAN_MODE_LOW_LATENCY = 2;
    public static final int SCAN_MODE_LOW_POWER = 0;
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;

    protected BluetoothAdapter mBluetoothAdapter;
    protected boolean mEnabled;
    protected BleScanListener mBleScanListener;

    // Only for API21
    protected int mScanMode = SCAN_MODE_BALANCED;

    //protected UUID[] mServices;


    public BleScannerEngine(Context context, BleScanListener bleScanListener) {
        mBleScanListener = bleScanListener;
        BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return;
        }

        if (enabled) {
            mEnabled = startScan();
        } else {
            stopScan();
            mEnabled = false;
        }
    }

    protected abstract boolean startScan();

    protected abstract void stopScan();


    public int getScanMode() {
        return mScanMode;
    }

    public void setScanMode(int scanMode) {
        mScanMode = scanMode;
    }

    public interface BleScanListener {
        void onBleScanDetected(final BluetoothDevice device, final int rssi, final byte[] scanRecord);
    }
}
