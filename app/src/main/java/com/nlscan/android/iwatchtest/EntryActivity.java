package com.nlscan.android.iwatchtest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;

import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;


import android.view.View;
import android.widget.Button;



public class EntryActivity extends AppCompatActivity {

    private Button begin;

    private String serviceName ="com.nlscan.android.iwatchtest.GPSService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        checkOff(null);
        begin = (Button) findViewById(R.id.btn_begin);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//
//                // 申请权限
//                ComponentName componentName = new ComponentName(EntryActivity.this, MyAdmin.class);
//                // 判断该组件是否有系统管理员的权限
//                boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
//                if(!isAdminActive){
                if (isServiceRunning()) stopService(new Intent(EntryActivity.this,GPSService.class));
                startActivity(new Intent(EntryActivity.this,MainActivity.class));

//                }
                //startActivity(new Intent(EntryActivity.this,MainActivity.class));
            }
        });
    }


    private void checkOff(View view){
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        // 申请权限
        ComponentName componentName = new ComponentName(EntryActivity.this, MyAdmin.class);
        // 判断该组件是否有系统管理员的权限
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
        if(!isAdminActive){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "开启后就可以使用锁屏功能了...");//显示位置见图二
            startActivityForResult(intent,0);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
