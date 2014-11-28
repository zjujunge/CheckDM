
package com.example.checkdm;

import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract.RawContacts.DisplayPhoto;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
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


import com.example.checkdm.R;

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

    GpsMo mGpsMo;
    BtMo mBtMo;
    DiagMon mDiagMon;
    NetMo mNetMo;

    String[] items = {"Battery Info", "GPS Info", "BT Info", "Enable GPS", "Diable GPS",
            "Enable BT", "Disable BT", "Enable Discoverable BT", "Disable Discoverable BT",
            "Ram Info", "Internal Storage", "External Storage", "Wifi Info", "Network Info",
            "Wifi Enable", "Wifi Disable", "Wifi Hotspot Enable", "Wifi Hostspot Disable",
            "Sound Info", "set Ringtone", "set Notification", "set Alarms",
            "set Audio Video Media", "set BlueTooth"};

    private int testLevel = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mGpsMo = new GpsMo(this);
        mBtMo = new BtMo(this);
        mDiagMon = new DiagMon(this);
        mNetMo = new NetMo(this);

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
        ListView mainList = (ListView) findViewById(R.id.mylistView);
        mainList.setAdapter(adapter);
        mainList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                switch (arg2) {
                    case 0:
                        _statusTv.setText(mDiagMon.getBatteryInfo());
                        break;
                    case 1:
                        _statusTv.setText(mGpsMo.getGpsInfo());
                        break;
                    case 2:
                        _statusTv.setText(mBtMo.getBtInfo());
                        break;
                    case 3:
                        mGpsMo.enableGps();
                        break;
                    case 4:
                        mGpsMo.disableGps();
                        break;
                    case 5:
                        _statusTv.setText(mBtMo.enableBt());
                        break;
                    case 6:
                        _statusTv.setText(mBtMo.disableBt());
                        break;
                    case 7:
                        _statusTv.setText(mBtMo.enableDiscoverableBt());
                        break;
                    case 8:
                        _statusTv.setText(mBtMo.disableDiscoverableBt());
                        break;
                    case 9: {
                        String[] array = mDiagMon.getRamMemory();
                        String ramInfo = "Ram total: " + array[0]
                                + " avail: " + array[1] + " used: " + array[2];
                        _statusTv.setText(ramInfo);
                        break;
                    }
                    case 10: {
                        String[] array = mDiagMon.getRomMemory();
                        String interInfo = "Internal total: " + array[0]
                                + " avail: " + array[1] + " used: " + array[2];
                        _statusTv.setText(interInfo);
                        break;
                    }
                    case 11: {
                        String[] array = mDiagMon.getSDCardMemory();
                        String externalInfo = "External total: " + array[0]
                                + " avail: " + array[1] + " used: " + array[2];
                        _statusTv.setText(externalInfo);
                        break;
                    }
                    case 12:
                        _statusTv.setText(mNetMo.getWifiInfo());
                        break;
                    case 13:
                        _statusTv.setText(mNetMo.getNetInfo());
                        break;
                    case 14:
                        _statusTv.setText("Enable Wifi");
                        mNetMo.setWifiEnabled(true);
                        break;
                    case 15:
                        _statusTv.setText("Disable Wifi");
                        mNetMo.setWifiEnabled(false);
                        break;
                    case 16:
                        _statusTv.setText("Enable Hotspot Wifi");
                        Log.d(TAG, "Enable Hotspot Wifi");
                        mNetMo.setWifiHotspotEnabled(true);
                        break;
                    case 17:
                        _statusTv.setText("Disable Hotspot Wifi");
                        Log.d(TAG, "Disable Hotspot Wifi");
                        mNetMo.setWifiHotspotEnabled(false);
                        break;
                    case 18:
                        _statusTv.setText(mDiagMon.getSoundInfo());
                        break;
                    case 19:
                        _statusTv.setText("setVolumesRingtone");
                        mDiagMon.setVolumesRingtone(10);
                        break;
                    case 20:
                        _statusTv.setText("setVolumesNotifications");
                        mDiagMon.setVolumesNotifications(20);
                        break;
                    case 21:
                        _statusTv.setText("setVolumesAlarms");
                        mDiagMon.setVolumesAlarms(30);
                        break;
                    case 22:
                        _statusTv.setText("setVolumesAudio_Video_Media");
                        mDiagMon.setVolumesAudio_Video_Media(40);
                        break;
                    case 23:
                        _statusTv.setText("setVolumesBluetooth");
                        mDiagMon.setVolumesBluetooth(50);
                        break;
                    case 24:
                        break;
                    case 25:
                        break;
                    case 26:
                        break;
                    case 27:
                        break;
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

    @Override
    public void finish() {
        mGpsMo.finish();
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

}
