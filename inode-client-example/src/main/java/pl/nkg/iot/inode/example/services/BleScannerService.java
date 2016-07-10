package pl.nkg.iot.inode.example.services;

import org.greenrobot.eventbus.EventBus;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import pl.nkg.iot.inode.android.BleScannerEngine;
import pl.nkg.iot.inode.android.BleScannerEngineFactory;
import pl.nkg.iot.inode.example.events.BleScanDetectedEvent;

public class BleScannerService extends Service implements BleScannerEngine.BleScanListener {

    static private final String EXTRA_SCAN_MODE = "scan_mode";

    public static void startService(Context context, int scanMode) {
        Intent intent = new Intent(context, BleScannerService.class);
        intent.putExtra(EXTRA_SCAN_MODE, scanMode);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, BleScannerService.class));
    }

    private BleScannerEngine mScannerEngine;

    public class LocalBinder extends Binder {
        public BleScannerService getService() {
            return BleScannerService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScannerEngine = new BleScannerEngineFactory(this, this).build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mScannerEngine.setScanMode(intent.getIntExtra(EXTRA_SCAN_MODE, BleScannerEngine.SCAN_MODE_BALANCED));
        mScannerEngine.setEnabled(true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mScannerEngine.setEnabled(false);
        super.onDestroy();
    }

    @Override
    public void onBleScanDetected(BluetoothDevice device, int rssi, byte[] scanRecord) {
        EventBus.getDefault().post(new BleScanDetectedEvent(device, rssi, scanRecord));
    }
}
