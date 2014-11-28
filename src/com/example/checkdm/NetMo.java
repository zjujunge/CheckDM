package com.example.checkdm;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.GetChars;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Refer to RadioInfo
 *
 */
public class NetMo {

    private static final String TAG = "NetMo";
    private Context mContext;
    private TelephonyManager mTelephonyManager;
    private ConnectivityManager mConnectivityManager;
    private WifiManager mWifiManager;
    private SignalStrength mSignalStrength;
    Object mSignalStrengthLock = new Object();
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength;
            mSignalStrengthLock.notify();
        }
    };

    public NetMo(Context context) {
        mContext = context;
        mTelephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mConnectivityManager =
                ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE));
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public String getNetInfo() {
        String netInfo;
        netInfo = "Net Info: is Roaming: " + isRoaming()
                + " MCC: " + getMcc() + " MNC: " + getMnc()
                + " Country ISO: " + getNetworkContryIso()
                + " SIM state: " + getNetworkSimState()
                + " Prefer Network Mode: " + getPreferredNetworkMode()
                + " Current Network Type: " + getCurrentNetwork()
                + " Call State: " + getCallState()
                + " Connect Type: " + getConnectType()
                + " Base Station Id: " + getBaseStationId()
                + " System Id: " + getSystemID()
                + " Network Id: " + getNetworkID()
                + " 3G signal: " + get3GSignal()
                + " 1x signal: " + get1xSignal()
                + " 4G signal: " + get4GSignal();
        return netInfo;
    }

    public String getWifiInfo() {
        String wifiInfo = "enabled: " + getWifiEnabled()
                + " SSID: " + getWifiSSID() + " BSSID: " + getWifiBssid()
                + " mac: " + getWifiMac() + " wifi status: " + getWifiStatus()
                + " signal: " + getWifiSignal()  + " speed: " + getWifiSpeed() + "dBm"
                + " detailed status: " + getWifiStatus() + "\n"
                + " wifi networks: " + getWifiNetworks();
        return wifiInfo;
    }

    private boolean isRoaming() {
        if (null == mTelephonyManager) {
            return false;
        }
        return mTelephonyManager.isNetworkRoaming();
    }

    private String getMcc() {
        String mcc = mTelephonyManager.getNetworkOperator();
        Log.d(TAG, "operator: " + mcc + " length: " + mcc.length());
        if ((null != mcc) && (mcc.length() == 5)) {
            mcc = mcc.substring(0, 3);
        } else {
            mcc = "0";
        }
        return mcc;
    }

    private String getMnc() {
        String mnc = mTelephonyManager.getNetworkOperator();
        if ((mnc != null) && (mnc.length() == 5)) {
            mnc = mnc.substring(3);
        } else {
            mnc = "0";
        }
        return mnc;
    }

    /**
     * Get on this node shall report the ISO Country Code of the current network
     * the device is connected to
     * If no value is available, report a value of N/A to the server
     * @return
     */
    private String getNetworkContryIso() {
        String iso = mTelephonyManager.getNetworkCountryIso();

        return iso;
    }

    private static final int[] SIM_STATE_MAP = {
        5 /*SIM_STATE_UNKNOWN*/, 0 /*SIM_STATE_ABSENT*/, 3 /*SIM_STATE_PIN_REQUIRED*/,
        4 /*SIM_STATE_PUK_REQUIRED*/, 2 /*SIM_STATE_NETWORK_LOCKED*/, 1 /*SIM_STATE_READY*/
    };
    /**
     * Get on this node shall report Integer value of the SIM State in the device.
     *
     * @return
     *      0: ABSENT
     *      1: READY
     *      2: NETOWK_LOCKED
     *      3: PIN_REQUIRED
     *      4: PUK_REQUIRED
     *      5: UNKNOWN
     */
    private int getNetworkSimState() {
        return SIM_STATE_MAP[mTelephonyManager.getSimState()];
    }

    /**
     * Get on this node shall return the RSSI value of 1xEVDO network in dBm
     * The value reported shall be in Integer format
     * If no value is available, report a value of 0 to the server.
     *
     * @return
     */
    private int get3GSignal() {
        mSignalStrength = null;
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        synchronized (mSignalStrengthLock) {
            while (mSignalStrength == null) {
                try {
                    mSignalStrengthLock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Toast.makeText(mContext, "get3GSignal: " + mSignalStrength.getCdmaDbm(),
                Toast.LENGTH_LONG);
        return mSignalStrength.getCdmaDbm();
    }

    /**
     * Get on this node shall return the RSSI value of the 1xRTT network in dBm
     * The value reported shall be in Integer format
     * If no value is available, report a value of 0 to the server.
     *
     * @return
     */
    private int get1xSignal() {
        mSignalStrength = null;
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        synchronized (mSignalStrengthLock) {
            while (mSignalStrength == null) {
                try {
                    mSignalStrengthLock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Toast.makeText(mContext, "get1xSignal: " + mSignalStrength.getGsmDbm(),
                Toast.LENGTH_LONG);
        return mSignalStrength.getGsmDbm();
    }

    /**
     * Get on this node shall return the RSSI value of the LTE network in dBm
     * The value reported shall be in Integer format
     * If no value is available, report a value of 0 to the server.
     *
     * @return
     */
    private int get4GSignal() {
        mSignalStrength = null;
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        synchronized (mSignalStrengthLock) {
            while (mSignalStrength == null) {
                try {
                    mSignalStrengthLock.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Toast.makeText(mContext, "get4GSignal: " + mSignalStrength.getLteDbm(),
                Toast.LENGTH_LONG);
        return mSignalStrength.getLteDbm();
    }

    private static final int NETWORK_TYPE_MAP[] = {
        -1 /*NETWORK_TYPE_UNKNOWN*/, 5 /*NETWORK_TYPE_GPRS*/, 6 /*NETWORK_TYPE_EDGE*/,
        7 /*NETWORK_TYPE_UMTS*/, -1 /*NETWORK_TYPE_CDMA*/, 1 /*NETWORK_TYPE_EVDO_0*/,
        2 /*NETWORK_TYPE_EVDO_A*/, 0 /*NETWORK_TYPE_1xRTT*/, 9 /*NETWORK_TYPE_HSDPA*/,
        9 /*NETWORK_TYPE_HSUPA*/, 8 /*NETWORK_TYPE_HSPA*/, -1 /*NETWORK_TYPE_IDEN*/,
        -1 /*NETWORK_TYPE_EVDO_B*/, 10 /*NETWORK_TYPE_LTE*/, 3 /*NETWORK_TYPE_EHRPD*/,
        -1 /*NETWORK_TYPE_HSPAP*/
    };
    /**
     * Get on this node shall return the Integer value of the current network
     * type the device is camped on
     *
     * @return
     *      0: 1xRTT
     *      1: 1xEVDO_Rev0
     *      2: 1xEVDO_RevA
     *      3: eHRPD
     *      4: GSM
     *      5: GPRS
     *      6: EDGE
     *      7: UMTS
     *      8: HSPA
     *      9: HSPA+
     *      10:LTE
     */
    private int getCurrentNetwork() {
        int type =  mTelephonyManager.getNetworkType();
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        type = networkInfo.getSubtype();
        Log.d(TAG, "TelephonyManager.getNetworkType: " + mTelephonyManager.getNetworkType()
                + " Current Network Info getSubtype: " + networkInfo.getSubtype());
        return NETWORK_TYPE_MAP[type];
    }

    private static final int[] CALL_STATE_MAP = {
        0 /*CALL_STATE_IDLE*/, 2 /*CALL_STATE_RINGING*/, 1 /*CALL_STATE_OFFHOOK*/
    };
    /**
     * Get on this node shall report the state of the phone.  Values shall be
     * reported in Integer format of the possible states
     *
     * @return
     *      0: IDLE
     *      1: OFFHOOK
     *      2: RINGING
     */
    private int getCallState() {
        return CALL_STATE_MAP[mTelephonyManager.getCallState()];
    }

    /**
     * Get on this node shall return the CDMA Base Station ID that the device
     * is connected to
     * If no value is available, report a value of 0 to the server.
     * @return
     */
    private int getBaseStationId() {
        int id = 0;
        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        if (cellLocation instanceof GsmCellLocation) {
            Log.d(TAG, "instanceof GsmCellLocation");
            GsmCellLocation location = (GsmCellLocation) cellLocation;
        } else if (cellLocation instanceof CdmaCellLocation) {
            Log.d(TAG, "instanceof CdmaCellLoaction");
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            id = location.getBaseStationId();
        }
        return id;
    }

    /**
     * Get on this node shall return the current CDMA System ID that the device
     * is connected to.
     * If no value is available, report a value of 0 to the server.
     * @return
     */
    private int getSystemID() {
        int id = 0;
        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        if (cellLocation instanceof GsmCellLocation) {
            Log.d(TAG, "instanceof GsmCellLocation");
            GsmCellLocation location = (GsmCellLocation) cellLocation;
        } else if (cellLocation instanceof CdmaCellLocation) {
            Log.d(TAG, "instanceof CdmaCellLoaction");
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            id = location.getSystemId();
        }
        return id;
    }

    /**
     * Get on this node shall report the current Network ID number of the CDMA
     * network the device is connected to
     * If no value is available, report a value of 0 to the server.
     *
     * @return
     */
    private int getNetworkID() {
        int id = 0;
        CellLocation cellLocation = mTelephonyManager.getCellLocation();
        if (cellLocation instanceof GsmCellLocation) {
            Log.d(TAG, "instanceof GsmCellLocation");
            GsmCellLocation location = (GsmCellLocation) cellLocation;
        } else if (cellLocation instanceof CdmaCellLocation) {
            Log.d(TAG, "instanceof CdmaCellLoaction");
            CdmaCellLocation location = (CdmaCellLocation) cellLocation;
            id = location.getNetworkId();
        }
        return id;
    }
    /**
     * Get on this node shall return the current network type the device is
     * connected to.
     *
     * @return
     *      0: WIFI
     *      1: MOBILE
     *      2: Unknown
     */
    private int getConnectType() {
        int type = 2;
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if ((networkInfo != null) && networkInfo.isAvailable()) {
            type = networkInfo.getType();
            if (type == 1) {
                type = 0;
            } else {
                type = 1;
            }
        } else {
            Log.w(TAG, "getConnectType fail");
        }
        return type;
    }

    /**
     * Get on this node shall return the integer value of the current Global
     * Data Roaming Access status.
     *
     * @return
     *      0: Deny Roaming Access
     *      1: Allow Roaming for the current trip
     *      2: Allow Roaming for all trip
     */
    private int getGlobalDataRoamingAccess() {
        // TODO:
        int access = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.CDMA_ROAMING_MODE, Phone.CDMA_RM_HOME);
        return access;
    }

    /**
     * Exec on this node shall result in the device setting Global Data Roaming
     * Access to Deny All Roaming and set the value of the
     * ./ManagedObjects/DiagMon/Network/GlobalDataRoamingAccess node to 0
     */
    private void execDenyRoaming() {
        // TODO:
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.CDMA_ROAMING_MODE, Phone.CDMA_RM_HOME);
    }

    /**
     * Exec on this node shall result in the device setting Global Data Roaming
     * Access to Allow Roaming only for the Current Trip and set the value of
     * the ./ManagedObjects/DiagMon/Network/GlobalDataRoamingAccess node to 1
     */
    private void execAllowCurrentTripRoaming() {
        // TODO:
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.CDMA_ROAMING_MODE, Phone.CDMA_RM_AFFILIATED);
    }

    /**
     * Exec on this node shall result in the device setting Global Data Roaming
     * Access to Allow All Roaming and set the value of the
     * ./ManagedObjects/DiagMon/Network/GlobalDataRoamingAccess node to 2
     */
    private void execAllowAllTripsRoaming() {
        // TODO:
        Settings.Global.putInt(mContext.getContentResolver(),
                Settings.Global.CDMA_ROAMING_MODE, Phone.CDMA_RM_ANY);
    }

    private static final int[] NETWORK_MODE_MAP = {
        2 /*NT_MODE_WCDMA_PREF*/, 5 /*NT_MODE_GSM_ONLY*/, 6 /*NT_MODE_WCDMA_ONLY*/,
        2 /*NT_MODE_GSM_UMTS*/, -1 /*NT_MODE_CDMA*/, -1 /*NT_MODE_CDMA_NO_EVDO*/,
        -1 /*NT_MODE_EVDO_NO_CDMA*/, -1 /*NT_MODE_GLOBAL*/, -1 /*NT_MODE_LTE_CDMA_AND_EVDO*/,
        3 /*NT_MODE_LTE_GSM_WCDMA*/, -1 /*NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA*/, 7 /*NT_MODE_LTE_ONLY*/,
        3 /*NT_MODE_LTE_WCDMA*/, 3 /*NT_MODE_LTE_GSM_GSM_PREF*/, 3/*NT_MODE_LTE_GSM_LTE_PREF*/,
        3 /*NT_MODE_LTE_GSM*/, 3 /*NT_MODE_LTE_WCDMA_WCDMA_PREF*/, 3 /*NT_MODE_LTE_WCDMA_LTE_PREF*/,
        -1 /**/, 3 /*NT_MODE_LTE_GSM_WCDMA_GSM_PREF*/, 3 /*NT_MODE_LTE_GSM_WCDMA_WCDMA_PREF*/,
        3 /*NT_MODE_LTE_GSM_WCDMA_LTE_PREF*/
    };

    /**
     * Get on this node shall return all the supported mode values comma separated.
     *
     * @return
     *      0: Global Mode
     *      1: LTE/CDMA Mode
     *      2: GSM/UMTS Mode
     *      3: LTE/Legacy 3GPP Mode
     *      4: CDMA Only Mode
     *      5: GSM Only Mode
     *      6: UMTS Only Mode
     *      7: LTE Only Mode
     */
    private String getSupportedModes() {
        return "2,3,5,6,7";
    }

    /**
     * Get on this node shall return the integer value of which Network Mode the
     * device is currently set to.
     *
     * @return
     *      0: Global Mode
     *      1: LTE/CDMA Mode
     *      2: GSM/UMTS Mode
     *      3: LTE/Legacy 3GPP Mode
     *      4: CDMA Only Mode
     *      5: GSM Only Mode
     *      6: UMTS Only Mode
     *      7: LTE Only Mode
     *
     */
    private int getPreferredNetworkMode() {
        Phone phone =  PhoneFactory.getDefaultPhone();
        Message msg = mHandler.obtainMessage(EVENT_QUERY_PREFERRED_TYPE_DONE);
        phone.getPreferredNetworkType(msg);
        int prefer = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.PREFERRED_NETWORK_MODE, 0);
        Log.d(TAG, "perferred network mode: " + prefer);
        return prefer;//return  NETWORK_MODE_MAP[prefer];
    }

    private void setPreferredNetworkType(int networkType) {
        Log.d(TAG, "setPreferredNetworkType: " + networkType);
        Phone phone =  PhoneFactory.getDefaultPhone();
        Message msg = mHandler.obtainMessage(EVENT_SET_PREFERRED_TYPE_DONE);
        phone.setPreferredNetworkType(networkType, msg);
    }

    /**
     * Exec on this node shall result in the device being put in to Global Mode
     * and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 0
     */
    private void execGlobal_Mode() {
        Log.w(TAG, "does not support");
    }

    /**
     * Exec on this node shall result in the device being put in to LTE/CDMA Mode
     * and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 1
     */
    private void execLTE_CDMA_Mode() {
        Log.w(TAG, "does not support");
    }

    /**
     * Exec on this node shall result in the device being put in to GSM/UMTS Mode
     * and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 2
     */
    private void execGSM_UMTS_Mode() {
        // TODO:
        setPreferredNetworkType(Phone.NT_MODE_GSM_UMTS);
    }

    /**
     * Exec on this node shall result in the device being put in to LTE/Legacy 3GPP
     * Mode and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 3
     */
    private void execLTE_Legacy3GPP_Mode() {
        // TODO:
        setPreferredNetworkType(Phone.NT_MODE_LTE_GSM_WCDMA);
    }

    /**
     * Exec on this node shall result in the device being put in to CDMA Only
     * Mode and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 4
     */
    private void execCDMA_Only_Mode() {
        Log.w(TAG, "does not support");
    }

    /**
     * Exec on this node shall result in the device being put in to GSM Only Mode
     * and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 5
     */
    private void execGSM_Only_Mode() {
        // TODO:
        setPreferredNetworkType(Phone.NT_MODE_LTE_ONLY);
    }

    /**
     * Exec on this node shall result in the device being put in to UMTS Only
     * Mode and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 6
     */
    private void execUMTS_Only_Mode() {
        // TODO:
        setPreferredNetworkType(Phone.NT_MODE_WCDMA_ONLY);
    }

    /**
     * Exec on this node shall result in the device being put in to LTE Only Mode
     * and the value of ./ManagedObjects/DiagMon/Network/Mode/PreferredNetworkMode
     * node shall be set to 7
     */
    private void execLTE_Only_Mode() {
        // TODO:
        setPreferredNetworkType(Phone.NT_MODE_GSM_UMTS);
    }

    private static final int EVENT_SET_PREFERRED_TYPE_DONE = 1;
    private static final int EVENT_QUERY_PREFERRED_TYPE_DONE = 2;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_SET_PREFERRED_TYPE_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        getPreferredNetworkMode();
                    }
                    break;
                case EVENT_QUERY_PREFERRED_TYPE_DONE:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        int type = ((int[]) ar.result)[0];
                        Log.d(TAG, "EVENT_QUERY_PREFERRED_TYPE_DONE type: " + type);
                    }
                    break;
                default:
                    break;
            }
        }
    };




    private boolean getWifiEnabled() {
        boolean enabled = false;
        //NetworkInfo networkInfo =
                //mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        enabled = mWifiManager.isWifiEnabled();//networkInfo.isAvailable();
        return enabled;
    }

    /**
     * Value of this node shall populate and change once the device connects to a WiFi
     * Network Get on this node shall report the Received Signal Strength of the currently
     * connected WiFi network in dBm.
     * The value shall be reported in Integer format.
     * If the device is not connected to a WiFi network, the device shall report a value of 0.
     *
     * @return
     */
    private int getWifiSignal() {
        int rssi = 0;
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            rssi = info.getRssi();
        }
        return rssi;
    }

    /**
     * Value of this node shall be populated when the device is connected to a WiFi
     * Network Get on this node shall report the speed in Mbps at which the WiFi
     * connection is connected at. The value shall be reported in Integer Format
     * If Device is not connected to a WiFi network, the device shall report a
     * value of 0
     *
     * @return
     */
    private int getWifiSpeed() {
        int speed = 0;
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            speed = info.getLinkSpeed();
        }
        return speed;
    }

    /**
     * Value of this node is populated once the device connects to a WiFi Network
     * Get on this node shall return the SSID the device is connected to.  Case
     * sensitivity of the broadcasted SSID shall be preserved
     * If the device is not connected to a WiFi network, then the device shall report value: N/A
     *
     * @return
     */
    private String getWifiSSID() {
        String ssid = "N/A";
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            ssid = info.getSSID();
        }
        return ssid;
    }

    /**
     * Get on this node shall report the MAC address of the WiFi adapter of the device
     * The value reported shall be in MAC address format i.e. ' XX:XX:XX:XX:XX:XX
     * All letters shall be reported in upper case
     *
     * @return
     */
    private String getWifiMac() {
        String mac = "00:00:00:00:00:00";
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            mac = info.getMacAddress();
        }
        return mac;
    }

    /**
     * Get on this node shall return the Basic Service Set ID value of the Access
     * point the device is connected to.
     * This is the MAC address of the Access Point that the device is connected to
     * The value reported shall be in MAC address format i.e. ' XX:XX:XX:XX:XX:XX
     * All letters shall be reported in upper case
     * If the device is not connected to an Access Point, the device shall return: 00:00:00:00:00:00
     *
     * @return
     */
    private String getWifiBssid() {
        String bssid = "00:00:00:00:00:00";
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            bssid = info.getBSSID();
        }
        return bssid;
    }

    // Map to VZW required wifi status
    private static final int[] WIFI_STATUS_MAP = {
        4 /*DISCONNECTED*/, 9 /*INTERFACE_DISABLED*/, 8 /*INACTIVE*/,
        11 /*SCANNING*/, 2 /*AUTHENTICATING*/, 1 /*ASSOCIATING*/,
        0 /*ASSOCIATED*/, 6 /*FOUR_WAY_HANDSHAKE*/, 7 /*GROUP_HANDSHAKE*/,
        3 /*COMPLETED*/, 5 /*DORMANT*/, 12 /*UNINITIALIZED*/, 10 /*INVALID*/
    };
    /**
     * Get on this node shall return the Integer value of the state WiFi adapter is in.  Possible values:
     *
     * @return
     *      0             ASSOCIATED
     *      1             ASSOCIATING
     *      2             AUTHENTICATING
     *      3             COMPLETED
     *      4             DISCONNECTED
     *      5             DORMANT
     *      6             FOUR_WAY_HANDSHAKE
     *      7             GROUP_HANDSHAKE
     *      8             INACTIVE
     *      9             INTERFACE_DISABLED
     *      10            INVALID
     *      11            SCANNING
     *      12            UNINITIALIZED
     */
    private int getWifiStatus() {
        //int state = mWifiManager.getWifiState();
        SupplicantState state = SupplicantState.INVALID;
        WifiInfo info = mWifiManager.getConnectionInfo();
        if (null != info) {
            state = info.getSupplicantState();
        }
        Log.d(TAG, "state: " + state);
        return WIFI_STATUS_MAP[state.ordinal()];
    }

    @Deprecated
    private DetailedState getWifiDetailedState() {
        DetailedState state;
        NetworkInfo networkInfo =
                mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        state = networkInfo.getDetailedState();
        return state;
    }

    /**
     * 1. Get on this node shall return the SSID of each of the Wi-Fi networks
     * that are visible to the device along with the RSSI values for those
     * networks and the Network Security option.
     * 2. To separate SSID, RSSI value, and Security Option for 1 Network ","
     * (comma ' without quotes) shall be used as a delimiter.
     * 3. There shall be no extra blank spaces between or around the values.
     * 4. Blank spaces in SSID Name(s) shall NOT be removed.
     * 5. SSID name(s) shall remain exactly (case and format preserved) as they
     * are sent by the Access Point
     * 6. For RSSI values, only the numbers shall be reported with maximum of 1
     * decimal place.  The word dBm shall not be reported as part of the result.
     * 7. For Network security option, possible values are:
     *      0    Unsecure
     *      1     WEP
     *      2     WPA
     *      3     WPA2 Personal
     *      4     WPA2 Enterprise
     * 8. To separate multiple networks "++" (two plus signs ' without quotes)
     * shall be used as delimiter.  There shall be no extra blank spaces between
     * or around the two values.
     * For example, if 3 networks are visible and:
     * If the SSID names are as follows: Sample SSID1, Sample_SSID2, and SampleSSID3
     * And their respective RSSI values are: -90dBm, -80.5dBm, -75dBm
     * And their respective Network Security options are: Non-Secure, WEP Protected,
     * and WPA2 personal
     * Then, the response from the device shall be: Sample SSID1,-90,0++Sample_SSID2,
     * -80.5,1++SampleSSID3,-75,3
     * 9. If no networks are available, then the device shall send back N/A,N/A,N/A
     * as the value for SSID, RSSI, and Security Option.
     * @return
     */
    private String getWifiNetworks() {
        StringBuilder networks = new StringBuilder("");
        /*List<WifiConfiguration> listWifi = mWifiManager.getConfiguredNetworks();
        if (null != listWifi) {
            String link = "";
            for (Iterator<WifiConfiguration> iterator  = listWifi.iterator();
                    iterator .hasNext();) {
                WifiConfiguration configuration = iterator.next();
                networks += link + configuration;
                link = "++";
            }
        } else {
            networks = "N/A,N/A,N/A";
        }*/
        List<ScanResult> listScan = mWifiManager.getScanResults();
        if (null != listScan) {
            String link = "";
            for (Iterator<ScanResult> iterator  = listScan.iterator();
                    iterator .hasNext();) {
                ScanResult scan = iterator.next();
                networks.append(link);
                networks.append(scan.SSID + ",");
                networks.append(scan.level + ",");
                networks.append(scan.capabilities);
                link = "++\n";
            }
        } else {
            networks.append("N/A,N/A,N/A");
        }
        return networks.toString();
    }

    private boolean getWifiHotspotEnabled() {
        boolean enabled = false;
        enabled = mWifiManager.isWifiApEnabled();
        return false;
    }

    // refer to Settings WifiApEnabler setSoftapEnabled
    public void setWifiHotspotEnabled(boolean enable) {
        final ContentResolver cr = mContext.getContentResolver();
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = mWifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
            mWifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (mWifiManager.setWifiApEnabled(null, enable)) {
            Log.d(TAG, "set Wifi Ap " + enable + " success");
            Toast.makeText(mContext, "set Wifi Ap " + enable + " success",
                    Toast.LENGTH_LONG).show();
        } else {
            Log.w(TAG, "set Wifi Ap " + enable + " fail!!!!");
            Toast.makeText(mContext, "set Wifi Ap " + enable + " fail!!!!",
                    Toast.LENGTH_LONG).show();
        }

        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }

    // refer to Settings WifiEnabler.
    public void setWifiEnabled(boolean enable) {
        // Disable tethering if enabling Wifi
        int wifiApState = mWifiManager.getWifiApState();
        if (enable && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
            mWifiManager.setWifiApEnabled(null, false);
        }

        //
        if (!mWifiManager.setWifiEnabled(enable)) {
            Log.w(TAG, "set Wifi " + enable + " fail!!!!");
            Toast.makeText(mContext, "set Wifi " + enable + " fail!!!!",
                    Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "set Wifi " + enable + " success");
            Toast.makeText(mContext, "set Wifi " + enable + " success",
                    Toast.LENGTH_LONG).show();
        }
    }
}
