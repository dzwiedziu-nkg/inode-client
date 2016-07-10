package pl.nkg.iot.inode.android;

import android.content.Context;
import android.os.Build;

public class BleScannerEngineFactory {
    private Context mContext;
    private BleScannerEngine.BleScanListener mListener;

    public BleScannerEngineFactory(Context context, BleScannerEngine.BleScanListener listener) {
        mContext = context;
        mListener = listener;
    }

    public BleScannerEngine build() {
        if (Build.VERSION.SDK_INT >= 21) {
            return new BleScannerEngine21(mContext, mListener);
        } else {
            return new BleScannerEngine18(mContext, mListener);
        }
    }
}
