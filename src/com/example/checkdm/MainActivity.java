
package com.example.checkdm;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract.RawContacts.DisplayPhoto;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


//import com.example.checkdm.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    private static final String TAG = "CheckDM";
    
    private static final String QUANTA_CHECK_NEW = "com.quanta.pobu.showdialog";
    private static final String QUANTA_CHECK_STATUS = "";
    
    private Context mContext;
    private Button _checkNewBtn;
    private Button _checkStatusBtn;
    private Button _showAlertBtn;
    private TextView _statusTv;
    
    private BroadcastReceiver mDmBroadcastReceiver = null;
    
    BluetoothAdapter mBtAdpater;
    LocationManager mLocationManager;
    private boolean fix_flag = false;
    
    private long ttff = -1;
    private int sv_num = 0;
    private String cno_msg = "";
    private String sv_msg = "";
    private double lat = -1;
    private double longitude = -1;
    private double alt = -1;
    private float velocity = -1;
    private float accuracy = -1;
    private int fix_cnt = 0; // counter to hold fix times
    private final static int MAX_CHANNEL_NUM = 32;
    private boolean register_flag = false;
    
    String[] items = {"Battery Info", "GPS Info", "BT Info", "Enable GPS", "Diable GPS",
            "Enable BT", "Disable BT", "Enable Discoverable BT", "Disable Discoverable BT"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mContext = this;
        
        mBtAdpater = BluetoothAdapter.getDefaultAdapter();
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        ArrayList<HashMap<String, Object>> lst = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        for (int i = 0; i < items.length; i++) {
            map = new HashMap<String, Object>();
            map.put("Img", R.drawable.version);
            map.put("Txt", items[i]);
            lst.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, lst, R.layout.list_content,
                new String[] {"Img", "Txt"},
                new int[] {R.id.button_logo, R.id.button_text});
        ListView mainList = (ListView) findViewById(R.id.listView);
        mainList.setAdapter(adapter);
        mainList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                switch (arg2) {
                    case 0:
                        getBatteryInfo();
                        break;
                    case 1:
                        getGpsInfo();
                        break;
                    case 2:
                        getBtInfo();
                        break;
                    case 3:
                        enableGps();
                        break;
                    case 4:
                        disableGps();
                        break;
                    case 5:
                        enableBt();
                        break;
                    case 6:
                        disableBt();
                        break;
                    case 7:
                        enableDiscoverableBt();
                        break;
                    case 8:
                        disableDiscoverableBt();
                    default:
                        break;
                }
            }
        });

        _statusTv = (TextView) findViewById(R.id.StatusTv);
        _checkNewBtn = (Button) findViewById(R.id.CheckNew);
        _checkNewBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                startCheckNew();
            }
        });

        _checkStatusBtn = (Button) findViewById(R.id.CheckStatus);
        _checkStatusBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                startCheckStatus();
            }
        });
        
        _showAlertBtn = (Button) findViewById(R.id.ShowAlert);
        _showAlertBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
//                new MyThread().start();
//                testshow();
                LinkAlert alert = new LinkAlert(getApplicationContext());
                alert.showAlert();
//                Context context = getApplicationContext();
//                Intent intent = new Intent(context, BackActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    

    private void startCheckNew() {
        Intent intent = new Intent();
        intent.setClassName("com.marvell.vdmc", "com.marvell.vdmc.VdmcManagerService");
        intent.putExtra("commandId", 130);
        mContext.startService(intent);
        _statusTv.setText("start Check New");
        registerDmBroadcastReceiver();
    }
    
    private void startCheckStatus() {
        Uri uri = Uri.parse("content://com.marvell.vdmcprovider/checkstatus");
        Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "start Check status cursor null");
        }
        while (cursor.moveToNext()) {
            String pacakgeName = cursor.getString(0);
            String resultCode = cursor.getString(1);
            String time = cursor.getString(2);
            String result = "package: " + pacakgeName + " result code: " + resultCode
                    + " time: " + time;
            _statusTv.setText("status result: " + result);
            Log.i(TAG, "package: " + pacakgeName + " result code: " + resultCode
                    + " time: " + time);
        }
        cursor.close();
    }
    
    private void registerDmBroadcastReceiver() {
        if (null == mDmBroadcastReceiver) {
            mDmBroadcastReceiver = new DMBroadcastReceiver();
            IntentFilter filter = new IntentFilter(QUANTA_CHECK_NEW);
            mContext.registerReceiver(mDmBroadcastReceiver, filter);
        }
    }

    private void unregisterDmBroadcastReceiver() {
        if (null != mDmBroadcastReceiver) {
            mContext.unregisterReceiver(mDmBroadcastReceiver);
            mDmBroadcastReceiver = null;
        }
    }
    
    private class DMBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceiver action: " + action);
            if (QUANTA_CHECK_NEW.equals(action)) {
                int dialog = intent.getIntExtra("dialog", 0);
                Log.i(TAG, "onReceiver dialog: " + dialog);
                _statusTv.setText("OnReceiver Dialog: " + dialog);
                unregisterDmBroadcastReceiver();
            }
        }
        
    }

    private int getBatteryInfo() {
        Intent batteryIntent = mContext.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int total = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int temp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int plugType = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1); // need convert.
        int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        String type = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

        String batterInfo = "level of battery is " + level + " total: " + total + " status: " + status
                + " temp: " + temp + " plugin: " + plugType + " voltage: " + voltage + " type: " + type;
        _statusTv.setText(batterInfo);
        level = (level * 100) / total;
        
        return level;
    }

    private void getGpsInfo() {
//        String allowed_location = Settings.Secure.getString(mContext.getContentResolver(),
//                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        String gpsInfo = "GPS Enabled: " + register_flag;
        _statusTv.setText(gpsInfo);
    }

    private void enableGps() {
//        Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
//                LocationManager.GPS_PROVIDER, true);
        register_flag = true;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                mLocationListener);
    }

    private void disableGps() {
//        Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
//                LocationManager.GPS_PROVIDER, false);
        register_flag = false;
        mLocationManager.removeUpdates(mLocationListener);
    }
    
    private final LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            updateLocation(location);
            if(fix_flag == true){
                longitude = location.getLongitude();
                alt = location.getAltitude();
                lat = location.getLatitude();
                velocity = location.getSpeed();
                accuracy = location.getAccuracy();
            }
        //  Log.i(LOG_TAG, "Location changed");
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
        if (null != location) {
            StringBuilder sb = new StringBuilder();
            sb.append("latitude: " + location.getLatitude() + "\n");
            sb.append("longitude: " + location.getLongitude() + "\n");
            sb.append("altitude: " + location.getAltitude() + "\n");
            sb.append("speed: " + location.getSpeed() + "\n");
            sb.append("accuracy: " + location.getAccuracy() + "\n");
        }
    }

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {

        private List<Map<String, Object>> sat_list = new ArrayList<Map<String, Object>>();
        private boolean fix = false;

        @Override
        public void onGpsStatusChanged(int event) {
        //  Log.i(LOG_TAG, "onGpsStatusChanged");
            GpsStatus gpsStatus = mLocationManager.getGpsStatus(null); // initial gpsatus
            switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "GPS started");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(TAG, "GPS stopped");
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
                sat_list.clear();
                while (iters2.hasNext() && cnt <= gpsStatus.getMaxSatellites() && cnt < MAX_CHANNEL_NUM) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    GpsSatellite oSat = (GpsSatellite) iters2.next();
                    map.put("sv", oSat.getPrn());
                    map.put("az", oSat.getAzimuth()); 
                    map.put("cn0", oSat.getSnr());
                    map.put("elev", oSat.getElevation());
                    sat_list.add(map); 
                    
                    cnt++;
                    msg_sv += oSat.getPrn();
                    msg_sv += "  ";
                    msg_cno += oSat.getSnr();
                    msg_cno += "  ";

                }
                if(register_flag == true){
                //  Log.i(LOG_TAG, "update sv/cno GUI");
                    //sortData(sat_list);
                    //adapter.notifyDataSetChanged();
                }
                
                if (fix == true) {
                    sv_num = cnt;
                    sv_msg = msg_sv;
                    cno_msg = msg_cno;
                    fix = false;
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

    
    private void getBtInfo() {
        String name = mBtAdpater.getName();
        boolean enabled = mBtAdpater.isEnabled();
        boolean discoveEnabled = mBtAdpater.isDiscovering();
        String btInfo = "name: " + name + " enabled: " + enabled
                + " discovable enabled: " + discoveEnabled;
        _statusTv.setText(btInfo);
    }
    
    private void enableBt() {
        if (mBtAdpater.getState() != BluetoothAdapter.STATE_ON) {
            boolean result = mBtAdpater.enable();
            _statusTv.setText("enable Bt result: " + result);
        } else {
            _statusTv.setText("bt already enabled");
        }
    }
    
    private void disableBt() {
        if (mBtAdpater.getState() != BluetoothAdapter.STATE_OFF) {
            boolean result = mBtAdpater.disable();
            _statusTv.setText("disable Bt result: " + result);
        } else {
            _statusTv.setText("bt already disabled");
        }
    }
    
    private void enableDiscoverableBt() {
        if (mBtAdpater.getState() != BluetoothAdapter.STATE_DISCONNECTING) {
            boolean result = mBtAdpater.startDiscovery();
            _statusTv.setText("enable Bt discoverable result: " + result);
        } else {
            _statusTv.setText("bt discoverable already enabled");
        }
    }
    
    private void disableDiscoverableBt() {
        if (mBtAdpater.getState() != BluetoothAdapter.STATE_DISCONNECTING) {
            boolean result = mBtAdpater.cancelDiscovery();
            _statusTv.setText("disable Bt discoverable result: " + result);
        } else {
            _statusTv.setText("bt discoverable already disabled");
        }
    }
}
