package com.example.checkdm;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GpsMo {

    private static final String TAG = "GpsMo";

    private Context mContext;
    LocationManager mLocationManager;
    private boolean fix_flag = false;

    private long ttff = -1;
    private int mSv_num = 0;
    private String cno_msg = "";
    private String sv_msg = "";
    private double lat = -1;
    private double longitude = -1;
    private double alt = -1;
    private float velocity = -1;
    private float accuracy = -1;
    private int fix_cnt = 0; // counter to hold fix times
    private final static int MAX_CHANNEL_NUM = 32;

    private static boolean sRegister_flg = false;
    private static boolean sGps_event_flg = false;

    private List<Map<String, Object>> mSat_list = new ArrayList<Map<String, Object>>();

    public GpsMo(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(mGpsStatusListener);
    }

    public void finish() {
        Log.i(TAG, "finish");
        if (sRegister_flg) {
            Log.i(TAG, "unregister Gps location listeners");
            mLocationManager.removeUpdates(mLocationListener);
        }
        Log.i(TAG, "unregister Gps status listeners");
        mLocationManager.removeGpsStatusListener(mGpsStatusListener);
    }

    private boolean isAllowGps() {
        String allowed_location = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.i(TAG, "get gps info allowed location: " + allowed_location);
        return "gps".equals(allowed_location) ? true : false;
    }

    public String getGpsInfo() {
        if (! isAllowGps()) {
            return "Gps not allowed";
        }
        String gpsInfo = "GPS Enabled: " + getGpsEnabled()
                + " Num of satellite: " + getNumOfSatellites()
                + " Snr In View Satellites: " + getSnrInViewSatellites();
        Log.d(TAG, "gpsInfo: " + gpsInfo);
        return gpsInfo;
    }

    public void enableGps() {
//        if (!isAllowGps()) {
//            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
//                    LocationManager.GPS_PROVIDER, true);
//        }
        Log.i(TAG, "Enable Gps");
        sRegister_flg = true;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                mLocationListener);
    }

    public void disableGps() {
        Log.i(TAG, "Disable Gps");
        sRegister_flg = false;
        mLocationManager.removeUpdates(mLocationListener);
//        Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
//                LocationManager.GPS_PROVIDER, false);
    }

    private boolean getGpsEnabled() {
        return isAllowGps(); // sRegister_flg sGps_event_flg
    }

    private int getNumOfSatellites() {
        return mSv_num;
    }

    private String getSnrInViewSatellites() {
        String snrs = "";
        Iterator<Map<String, Object>> it = mSat_list.iterator();
        while (it.hasNext()) {
            Map<String, Object> map = it.next();
            String snr = map.get("cn0").toString();
            snrs += snr + ", ";
        }
        Log.i(TAG, "getSnrInViewSatellites: " + snrs);
        return snrs;
    }

    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
            if (fix_flag == true){
                longitude = location.getLongitude();
                alt = location.getAltitude();
                lat = location.getLatitude();
                velocity = location.getSpeed();
                accuracy = location.getAccuracy();
            }
            Log.i(TAG, "Location changed");
        }


        //GPS disabled
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
            Log.i(TAG, "GPS disabled");
        }

        //GPS Enabled
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
            Log.i(TAG, "GPS enabled");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    };


    private void updateLocation(Location location) {
        Log.i(TAG, "update location");
        if (null != location) {
            StringBuilder sb = new StringBuilder();
            sb.append("latitude: " + location.getLatitude() + "\n");
            sb.append("longitude: " + location.getLongitude() + "\n");
            sb.append("altitude: " + location.getAltitude() + "\n");
            sb.append("speed: " + location.getSpeed() + "\n");
            sb.append("accuracy: " + location.getAccuracy() + "\n");
            Log.i(TAG, "updateLocation: \n" + sb.toString());
        }
    }

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {
        private boolean fix = false;

        @Override
        public void onGpsStatusChanged(int event) {
            Log.i(TAG, "onGpsStatusChanged event: " + event);
            GpsStatus gpsStatus = mLocationManager.getGpsStatus(null); // initial gpsatus
            switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "GPS started");
                sGps_event_flg = true;
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(TAG, "GPS stopped");
                sGps_event_flg = false;
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                if(ttff == -1){
                    ttff = gpsStatus.getTimeToFirstFix();
                    fix_cnt++;
                    Log.i(TAG, "ttff time is : " + ttff);
                    updateTTFF(ttff);
                    fix = true;
                    fix_flag = true;
                }
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
            //  Log.i(LOG_TAG, "GPS satellite status changed");
                Iterator<GpsSatellite> iters2 = gpsStatus.getSatellites()
                        .iterator();
                String msg_sv = "";
                String msg_cno = "";

                int cnt = 0;
                mSat_list.clear();
                while (iters2.hasNext() && cnt <= gpsStatus.getMaxSatellites() && cnt < MAX_CHANNEL_NUM) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    GpsSatellite oSat = (GpsSatellite) iters2.next();
                    map.put("sv", oSat.getPrn());
                    map.put("az", oSat.getAzimuth());
                    map.put("cn0", oSat.getSnr());
                    map.put("elev", oSat.getElevation());
                    mSat_list.add(map);

                    cnt++;
                    msg_sv += oSat.getPrn();
                    msg_sv += "  ";
                    msg_cno += oSat.getSnr();
                    msg_cno += "  ";

                }
                if(sRegister_flg == true){
                    //Log.i(TAG, "update sv/cno GUI");
                    //sortData(mSat_list);
                }
                mSv_num = cnt;
                if (fix == true) {
                    // mSv_num = cnt;
                    sv_msg = msg_sv;
                    cno_msg = msg_cno;
                    fix = false;
                    Log.i(TAG, "mSv_num: " + mSv_num + " sv_msg: " + sv_msg
                            + " cno_msg: " + cno_msg);
                }
                break;
            default:
                break;
            }
        }
    };

    private void updateTTFF(long fftime) {
        Log.d(TAG, "updateTTFF: " + fftime);
    }

    private void sortData(List<Map<String, Object>> list) {
        if (!list.isEmpty()){
           Collections.sort(list, new Comparator<Map<String, Object>>() {
               @Override
               public int compare(Map<String, Object> object1, Map<String,Object> object2) {
                   //sort according to "cn0"
                   return object2.get("cn0").toString()
                           .compareTo(object1.get("cn0").toString());
               }
           });
       }
   }

}
