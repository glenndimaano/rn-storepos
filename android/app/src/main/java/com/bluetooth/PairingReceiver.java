package com.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class PairingReceiver extends BroadcastReceiver {
    
    public final String TAG = "RTNBluetoothModule";

    Callback mCallback;

    PairingReceiver(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
            final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                    BluetoothDevice.ERROR);
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (state == BluetoothDevice.BOND_BONDED) {

                Log.d(TAG, String.format("Successfully paired with device %s", device.getAddress()));
                mCallback.onSuccess(device);
                context.unregisterReceiver(this);

            } else if (state == BluetoothDevice.BOND_BONDING) {

                Log.d(TAG, "Bonding..");
            } else if (state == BluetoothDevice.BOND_NONE) {

                Log.d(TAG, String.format("Completed un-pairing with device %s", device.getAddress()));
                mCallback.onSuccess(device);
                context.unregisterReceiver(this);
            }
        }
    }

    public IntentFilter intent() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        return filter;
    }

    public interface Callback {

        void onSuccess(BluetoothDevice device);
    
        void onFailed(Exception e);
    }
}