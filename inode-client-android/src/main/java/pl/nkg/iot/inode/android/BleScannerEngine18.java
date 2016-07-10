package pl.nkg.iot.inode.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class BleScannerEngine18 extends BleScannerEngine {

    BleScannerEngine18(Context context, BleScanListener bleScanListener) {
        super(context, bleScanListener);
    }

    @Override
    protected boolean startScan() {
        //if (mServices == null) {
        return mBluetoothAdapter.startLeScan(mLeScanCallback);
        //} else {
        //    return mBluetoothAdapter.startLeScan(mServices, mLeScanCallback);
        //}
    }

    @Override
    protected void stopScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    mBleScanListener.onBleScanDetected(device, rssi, scanRecord);
                }
            };
}
