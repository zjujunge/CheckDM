package com.example.checkdm;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class BtMo {

    private static final String TAG = "BtMo";
    private Context mContext;
    BluetoothAdapter mBtAdpater;

    // Convert State to VZW State
    private static final int[] STATE_MAP = {2, 1, 0, 3};

    public BtMo(Context context) {
        mContext = context;

        mBtAdpater = BluetoothAdapter.getDefaultAdapter();
    }

    public String getBtInfo() {
        String btInfo = "name: " + getBtName()
                + "\nenabled: " + getBtEnabled()
                + "\ndiscovable enabled: " + getBtDiscoverableEnabled()
                + "\nHDP state: " + getBtHDPState()
                + "\nA2DP state: " + getBtA2DPState()
                + "\nHSP state: " + getBtHSPState();
        return btInfo;
    }

    public String enableBt() {
        String str = "";
        if (!mBtAdpater.isEnabled()) {
            boolean result = mBtAdpater.enable();
            str = "enable Bt result: " + result;
        } else {
            str = "bt already enabled";
        }
        return str;
    }

    public String disableBt() {
        String str = "";
        if (mBtAdpater.isEnabled()) {
            boolean result = mBtAdpater.disable();
            str = "disable Bt result: " + result;
        } else {
            str = "bt already disabled";
        }
        return str;
    }

    public String enableDiscoverableBt() {
        String str = "";
        if (!mBtAdpater.isDiscovering()) {
            boolean result = mBtAdpater.startDiscovery();
            str = "enable Bt discoverable result: " + result;
        } else {
            str = "bt discoverable already enabled";
        }
        return str;
    }

    public String disableDiscoverableBt() {
        String str = "";
        if (mBtAdpater.isDiscovering()) {
            boolean result = mBtAdpater.cancelDiscovery();
            str = "disable Bt discoverable result: " + result;
        } else {
            str = "bt discoverable already disabled";
        }
        return str;
    }

    private boolean getBtEnabled() {
        return mBtAdpater.isEnabled();
    }

    private boolean getBtDiscoverableEnabled() {
        return mBtAdpater.isDiscovering();
    }

    private String getBtName() {
        return mBtAdpater.getName();
    }

    /**
     * Get on this node shall report the Integer value of the state of the
     * Health Device Profile of the device.
     * @return 0: CONNECTED
     *         1: CONNECTING
     *         2: DISCONNECTED
     *         3: DISCONNECTING
     */
    private int getBtHDPState() {
        int state = mBtAdpater.getProfileConnectionState(BluetoothProfile.HEALTH);
        return STATE_MAP[state];
    }

    /**
     * Get on this node shall provide the Integer value of the status of
     * the Advanced Audio Distribution profile of the device.
     * @return 0: CONNECTED
     *         1: CONNECTING
     *         2: DISCONNECTED
     *         3: DISCONNECTING
     */
    private int getBtA2DPState() {
        int state = mBtAdpater.getProfileConnectionState(BluetoothProfile.A2DP);
        return STATE_MAP[state];
    }

    /**
     * Get on this node shall provide the Headset profile state of the device.
     * @return 0: CONNECTED
     *         1: CONNECTING
     *         2: DISCONNECTED
     *         3: DISCONNECTING
     */
    private int getBtHSPState() {
        int state = mBtAdpater.getProfileConnectionState(BluetoothProfile.HEADSET);
        return STATE_MAP[state];
    }
}
