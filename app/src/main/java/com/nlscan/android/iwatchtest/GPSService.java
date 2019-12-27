package com.nlscan.android.iwatchtest;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class GPSService extends Service {




    private String locateType;


    private LocationManager locationManager;


    private final IBinder mBinder = new GPSServiceBinder();



    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (GPSService.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(GPSService.this, "GPS权限未开启!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
        locationManager.requestLocationUpdates(locateType, 60*1000,0,
                locationListener);
    }


    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(GPSService.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GPSService.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(GPSService.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(GPSService.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
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
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //判断是否开启GPS定位功能

        //定位类型：GPS
        locateType = locationManager.GPS_PROVIDER;
        getLocation();
        Log.v(Constants.TAG, "GPSService Started.");
    }



    @Override
    public void onDestroy()
    {
        endService();
        Log.v(Constants.TAG, "GPSService Ended.");
    }

    private class GPSServiceBinder extends Binder {
        GPSService getService() {
            return GPSService.this;
        }
    }
}
