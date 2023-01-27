package com.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class DiscoveryReceiver extends BroadcastReceiver {

    public final String TAG = "RTNBluetoothModule";

    private Map<String, BluetoothDevice> unpairedDevices;

    private Callback mCallback;

    DiscoveryReceiver(Callback callback) {
        mCallback = callback;
        unpairedDevices = new HashMap<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            mCallback.onDiscovering(true);
        } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (!unpairedDevices.containsKey(device.getAddress())) {
                unpairedDevices.put(device.getAddress(), device);
                mCallback.onFoundDevice(device);
            }

        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            mCallback.deviceList(unpairedDevices.values());

            mCallback.onDiscovering(false);
            context.unregisterReceiver(this);
        }
    }

    public IntentFilter intent() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        return filter;
    }

    public interface Callback {

        void onFoundDevice(BluetoothDevice device);

        void deviceList(@Nullable Collection<BluetoothDevice> devices);

        void onDiscovering(Boolean discovering);

        void onFailed(Exception e);

    }
}