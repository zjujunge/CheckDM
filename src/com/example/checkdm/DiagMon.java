package com.example.checkdm;

import android.R.integer;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;

public class DiagMon {
    private static final String TAG = "DiagMon";
    Context mContext;

    private static final int INDEX_TOATL = 0;
    private static final int INDEX_AVAIL = 1;
    private static final int INDEX_USED = 2;

    AudioManager mAudioManager;

    public DiagMon(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public String getBatteryInfo() {
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
//        level = (level * 100) / total;

        return batterInfo;
    }

 // Get internal memory info.
    public String[] getRomMemory() {
        long[] romInfo = new long[3];
        String[] strRomInfo = new String[3];
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSize();
        long totalBlocks = statFs.getBlockCount();
        long availBlocks = statFs.getAvailableBlocks();
        // Total
        romInfo[INDEX_TOATL] = totalBlocks * blockSize;
        // Available
        romInfo[INDEX_AVAIL] = availBlocks * blockSize;
        // Used
        romInfo[INDEX_USED] = (totalBlocks - availBlocks) * blockSize;

        for (int i = 0; i < 3; i++) {
            strRomInfo[i] = Formatter.formatFileSize(mContext, romInfo[i]);
        }
        return strRomInfo;
    }

    // Get external memory info.
    // this method get internal storage
    @Deprecated
    public String[] getSDCardMemory() {
        long[] sdCardInfo = new long[3];
        String[] strSdCardInfo = new String[3];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdCardDir = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(sdCardDir.getPath());
            long blockSize = statFs.getBlockSize();
            long totalBlocks = statFs.getBlockCount();
            long availBlocks = statFs.getAvailableBlocks();
            // Total
            sdCardInfo[INDEX_TOATL] = totalBlocks * blockSize;
            // Available
            sdCardInfo[INDEX_AVAIL] = availBlocks * blockSize;
            // Used
            sdCardInfo[INDEX_USED] = (totalBlocks - availBlocks) * blockSize;

            for (int i = 0; i < 3; i++) {
                strSdCardInfo[i] = Formatter.formatFileSize(mContext,
                                sdCardInfo[i]);
            }
            return strSdCardInfo;
        }
        return null;
    }

    public String[] getRamMemory() {
        long[] ramInfo = new long[3];
        String[] strRamInfo = new String[3];
        ActivityManager am = (ActivityManager)mContext.
                getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        ramInfo[INDEX_TOATL] = mi.totalMem;
        ramInfo[INDEX_AVAIL] = mi.availMem;
        ramInfo[INDEX_USED] = mi.totalMem - mi.availMem;
        for (int i = 0; i < 3; i++) {
            strRamInfo[i] = Formatter.formatFileSize(mContext,
                            ramInfo[i]);
        }
        return strRamInfo;
    }

    // Get ram avail memory.
    public String getAvailMemory() {
        ActivityManager am = (ActivityManager)mContext.
                getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return Formatter.formatFileSize(mContext, mi.availMem);
    }

    // Get ram total memory.
    public String getTotalMemory() {
        String strMemInfo = "/proc/meminfo";
        String strLine;
        String[] arrayOfString;
        long initMem = 0;
        try {
            FileReader localFileReader = new FileReader(strMemInfo);
            BufferedReader localBufferedReader =
                    new BufferedReader(localFileReader, 8192);
            // Read first line.
            strLine = localBufferedReader.readLine();
            arrayOfString = strLine.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(TAG, strLine + " " + num + "\t");
            }
            // Get kb.
            initMem = Integer.valueOf(arrayOfString[1]).intValue()*1024;
            localBufferedReader.close();
        } catch (Exception e) {

        }
        return Formatter.formatFileSize(mContext, initMem);
    }

    public String getSoundInfo() {
        String soundInfo = "ringstone: " + getVolumesRingtone()
                + " notification: " + getVolumesNotifications()
                + " alarm: " + getVolumesAlarms()
                + " audio_video_media: " + getVolumesAudio_Video_Media()
                + " bluetooth: " + getVolumesBluetooth();
        return soundInfo;
    }

    private int getVolumesRingtone() {
        int level = 0;
        level = getStreamVolume(AudioManager.STREAM_RING);
        return level;
    }

    private int getVolumesNotifications() {
        int level = 0;
        level = getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        return level;
    }

    private int getVolumesAlarms() {
        int level = 0;
        level = getStreamVolume(AudioManager.STREAM_ALARM);
        return level;
    }

    private int getVolumesAudio_Video_Media() {
        int level = 0;
        level = getStreamVolume(AudioManager.STREAM_SYSTEM);
        return level;
    }

    private int getVolumesBluetooth() {
        int level = 0;
        level = getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO);
        return level;
    }

    // level rang 0-100
    public void setVolumesRingtone(int level) {
        setStreamVolume(AudioManager.STREAM_RING, level);
    }

    // level rang 0-100
    public void setVolumesNotifications(int level) {
        setStreamVolume(AudioManager.STREAM_NOTIFICATION, level);
    }

    // level rang 0-100
    public void setVolumesAlarms(int level) {
        setStreamVolume(AudioManager.STREAM_ALARM, level);
    }

    // level rang 0-100
    public void setVolumesAudio_Video_Media(int level) {
        setStreamVolume(AudioManager.STREAM_SYSTEM, level);
    }

    // level rang 0-100
    public void setVolumesBluetooth(int level) {
        setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, level);
    }

    // level range 0-100
    private int getStreamVolume(int streamtype) {
        int level = 0;
        int index = mAudioManager.getStreamVolume(streamtype);
        int max = mAudioManager.getStreamMaxVolume(streamtype);
        // convert to 0-100
        float tmp = index;
        tmp = tmp * 100 / max;
        level = (int) tmp;
        return level;
    }

    // level rang 0-100
    private void setStreamVolume(int streamType, int level) {
        int index = 0;
        int max = mAudioManager.getStreamMaxVolume(streamType);
        float tmp = level * max / 100;
        index = (int) tmp;
        mAudioManager.setStreamVolume(streamType, index, AudioManager.FLAG_PLAY_SOUND);
    }
}
