package com.bluetooth;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;

import java.util.Collection;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

import com.bluetooth.DiscoveryReceiver;
import com.bluetooth.PairingReceiver;

public class BluetoothModule extends ReactContextBaseJavaModule
        implements LifecycleEventListener, ActivityEventListener {

    public final String TAG = "RTNBluetoothModule";
    private final String APP_NAME = "RTNBluetooth";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private ReactApplicationContext mContext;
    private BluetoothAdapter mAdapter;
    private DiscoveryReceiver mDiscoveryReceiver;
    private PairingReceiver mPairingReceiver;

    BluetoothModule(ReactApplicationContext context) {
        super(context);

        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        this.getReactApplicationContext().addLifecycleEventListener(this);
        this.getReactApplicationContext().addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return APP_NAME;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onActivityResult requestCode: %d resultCode: %d", requestCode, resultCode));
        
        WritableMap params = Arguments.createMap();

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                params.putBoolean("state", true);
                sendEvent("onStateChange", params);
            } else if ( resultCode == Activity.RESULT_CANCELED) {
                params.putBoolean("state", false);
                sendEvent("onStateChange", params);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: " + intent.getAction());
    }

    @Override
    public void onHostResume() {
        Log.d(TAG, "onHostResume: register Application receivers");
    }

    @Override
    public void onHostPause() {
        Log.d(TAG, "onHostPause: unregister receivers");
    }

    @Override
    public void onHostDestroy() {
        Log.d(TAG, "onHostDestroy: stop discovery, connections and unregister receivers");
        mContext.unregisterReceiver(mDiscoveryReceiver);
    }

    @ReactMethod
    public void addListener(String eventName) {
        // Set up any upstream listeners or background tasks as necessary
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Remove upstream listeners, stop unnecessary background tasks
    }

    @ReactMethod
    public void isBluetoothEnabled(Promise promise) {
        if (mAdapter == null) {
            promise.reject("Bluetooth is not available");
            return;
        }
        promise.resolve(mAdapter.isEnabled());
    }

    @ReactMethod
    public void bluetoothEnabledOrDisabled(Promise promise) {

        if (mAdapter.isEnabled()) {
            WritableMap params = Arguments.createMap();
            params.putBoolean("state", false);
            sendEvent("onStateChange", params);
            mAdapter.disable();
            return;
        }

        Activity activity = getCurrentActivity();
        try {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        } catch (ActivityNotFoundException e) {
            promise.reject(e);

        }
    }

    @ReactMethod
    public void getBondedDevices(Promise promise) {
        if (mAdapter == null) {
            promise.reject("Bluetooth is not available");
            return;
        }

        if (!mAdapter.isEnabled()) {
            promise.reject("Bluetooth adapter is Off");
            return;
        }

        WritableArray deviceList = Arguments.createArray();
        for (BluetoothDevice rawDevice : mAdapter.getBondedDevices()) {
            deviceList.pushMap(createDeviceMap(rawDevice));
        }
        promise.resolve(deviceList);
    }

    @ReactMethod
    public void startDiscovery(Promise promise) {
        if (mAdapter == null) {
            promise.reject("Bluetooth is not available");
            return;
        }

        if (!mAdapter.isEnabled()) {
            promise.reject("Bluetooth is turn off");
            return;
        }

        if (mAdapter.isDiscovering()) {
            promise.reject("Already discovering");
            return;
        }

        mDiscoveryReceiver = new DiscoveryReceiver(new DiscoveryReceiver.Callback() {

            @Override
            public void onDiscovering(Boolean discovering) {
                Log.d(TAG, "SendEvent onDiscovering : " + discovering);

                WritableMap params = Arguments.createMap();
                params.putBoolean("state", discovering);
                sendEvent("onDiscovering", params);
            }

            @Override
            public void onFoundDevice(BluetoothDevice device) {
                Log.d(TAG, "SendEvent onDeviceFound : " + device);

                WritableMap params = Arguments.createMap();
                params.putString("device", device.getAddress());
                sendEvent("onDeviceFound", params);
            }

            @Override
            public void deviceList(Collection<BluetoothDevice> devices) {
                WritableArray deviceList = Arguments.createArray();
                for (BluetoothDevice device : devices) {
                    deviceList.pushMap(createDeviceMap(device));
                }
                promise.resolve(deviceList);
                mDiscoveryReceiver = null;
            }

            @Override
            public void onFailed(Exception e) {
                mAdapter.cancelDiscovery();
                mDiscoveryReceiver = null;
            }
        });

        mContext.registerReceiver(mDiscoveryReceiver, mDiscoveryReceiver.intent());
        mAdapter.startDiscovery();
    }

    @ReactMethod
    public void pairToDevice(String address, Promise promise) {
        if (mAdapter == null) {
            promise.reject("Bluetooth is not available");
            return;
        }

        if (!mAdapter.isEnabled()) {
            promise.reject("Bluetooth is turn off");
            return;
        }

        mPairingReceiver = new PairingReceiver(
                new PairingReceiver.Callback() {
                    @Override
                    public void onSuccess(BluetoothDevice device) {
                        promise.resolve(createDeviceMap(device));
                    }

                    @Override
                    public void onFailed(Exception e) {
                        promise.reject(e);
                    }
                });

        mContext.registerReceiver(mPairingReceiver, mPairingReceiver.intent());

        Log.d(TAG, String.format("Attempting to pair with device %s", address));
        try {
            BluetoothDevice device = mAdapter.getRemoteDevice(address);
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void unpairToDevice(String address, Promise promise) {
        try {
            Log.d(TAG, String.format("Attempting to unpair with device %s", address));
            BluetoothDevice device = mAdapter.getRemoteDevice(address);
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private WritableMap createDeviceMap(BluetoothDevice mDevice) {
        WritableMap props = Arguments.createMap();
        props.putString("name", mDevice.getName() != null ? mDevice.getName() : "Null");
        props.putString("address", mDevice.getAddress());
        props.putString("id", mDevice.getAddress());
        props.putBoolean("bonded", mDevice.getBondState() == BluetoothDevice.BOND_BONDED);

        return props;
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}
