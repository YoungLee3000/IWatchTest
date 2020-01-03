package com.nlscan.android.iwatchtest;

import android.Manifest;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GPSService extends Service {


    private final IBinder mBinder = new GPSServiceBinder();
    private Handler mHandler = null;

    //文件信息
    private String mPath = "/sdcard/IWatchTest/";
    private String mFileName = "Battery.txt";

    //电量与时间信息
    static final int UPDATE_INTERVAL = 1000*60;
    private int mPercent = 0;
    private long mInitTime = 0L;
    private long mSleepTime = 1000 * 60L * 60 * 8;// * 60 * 8L;

    //定位信息
    private String locateType;
    private LocationManager locationManager;
    private double mLongitude = 0.0;
    private double mLatitude = 0.0;
    private double mAltitude = 0.0;




    private void setLocationManager() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (GPSService.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "GPS权限未开启！" );
            toastUtil("GPS权限未开启!");
            return;
        }
        //Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米

        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*60, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60, 0, locationListener);
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (GPSService.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "GPS权限未开启！" );
            Toast.makeText(GPSService.this, "GPS权限未开启!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.i(Constants.TAG, "开始获取GPS信息!" );
        Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        if (location != null){
            mLongitude = location.getLongitude();
            mLatitude = location.getLatitude();
            mAltitude = location.getAltitude();
            Log.i(Constants.TAG, "时间：" + location.getTime());
            Log.i(Constants.TAG, "经度：" + location.getLongitude());
            Log.i(Constants.TAG, "纬度：" + location.getLatitude());
            Log.i(Constants.TAG, "海拔：" + location.getAltitude());
        }


    }


    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            toastUtil("触发定位！");
            mLongitude = location.getLongitude();
            mLatitude = location.getLatitude();
            mAltitude = location.getAltitude();
            Log.i(Constants.TAG, "时间：" + location.getTime());
            Log.i(Constants.TAG, "经度：" + location.getLongitude());
            Log.i(Constants.TAG, "纬度：" + location.getLatitude());
            Log.i(Constants.TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    toastUtil("onStatusChanged：当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    toastUtil("onStatusChanged:当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    toastUtil("onStatusChanged:当前GPS状态为暂停服务状态");
                   break;
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(GPSService.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
            getLocation();
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    public void endService()
    {
        if(locationManager != null && locationListener != null)
        {
            locationManager.removeUpdates(locationListener);
        }
    }



    public GPSService() {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onCreate()
    {


        Log.v(Constants.TAG, "GPSService Started.");

        //初始创建文件
        FileUtil.createDir(mPath);
        FileUtil.createFile(mPath + mFileName);
        mInitTime = System.currentTimeMillis();

        //电量监控
        registerReceiver(mBatteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        //GPS相关
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locateType = locationManager.NETWORK_PROVIDER; //定位类型：GPS
        setLocationManager();




        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run()
            {
                getLocation();
                Date date = new Date();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeLog = "当前时间: " + df.format(date) + " 当前电量： " + mPercent + "%" +
                                  " 经度: " + mLongitude  + " 纬度: " + mLatitude + " 海拔: "  + mAltitude + "\n";
                FileUtil.writeText(mPath + mFileName,timeLog);
                if (System.currentTimeMillis() - mInitTime >= mSleepTime){
                    Intent intent1 = new Intent(GPSService.this,ScreenOff.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);

                }else{
                    mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        });


    }


    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");// 获得当前电量
            int total = intent.getExtras().getInt("scale");// 获得总电量
            mPercent = current * 100 / total;
        }
    };


    @Override
    public void onDestroy()
    {
        unregisterReceiver(mBatteryReceiver);
        endService();
        Log.v(Constants.TAG, "GPSService Ended.");
    }

    private class GPSServiceBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
    }


    private void toastUtil(final String str){
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            public void run(){
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
