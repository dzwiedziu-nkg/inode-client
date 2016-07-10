package pl.nkg.iot.inode.android;

import android.annotation.TargetApi;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

@TargetApi(21)
public class BleScannerEngine21 extends BleScannerEngine {

    private BluetoothLeScanner mScanner;

    BleScannerEngine21(Context context, BleScanListener bleScanListener) {
        super(context, bleScanListener);
        mScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @Override
    protected boolean startScan() {
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(mScanMode)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        mScanner.startScan(filters, settings, mScanCallback);
        return true;
    }

    @Override
    protected void stopScan() {
        mScanner.stopScan(mScanCallback);
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ScanRecord record = result.getScanRecord();
            byte[] data = record == null ? null : record.getBytes();
            mBleScanListener.onBleScanDetected(result.getDevice(), result.getRssi(), data);
        }
    };
}
