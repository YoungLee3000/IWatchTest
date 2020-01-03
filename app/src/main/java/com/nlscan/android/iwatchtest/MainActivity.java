package com.nlscan.android.iwatchtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {



    private Handler mHandler;
    //UI相关
    UIHandler mUIHandler = new UIHandler(this);
    private static final int CASE_TAKEPHOTO = 1;
    private static final int CASE_UPLOAD = 2;
    private static final int CASE_DOWNLOAD = 3;
    private static final int CASE_VIBRATION = 4;

    //下载相关
    private long mDownLoadId = 0;
    private boolean mIfDownloadComplete = false;
    private TextView textDowC;

    //上传相关
    private static String mActionUrl = "http://218.66.48.235:5000/tmp.asp";
    private  int mPassFlag = 0;
    private int mFailFlag = 0;
    private  int mUploadFlag = 1;
    private long DATA_SIZE = 1024 * 1024 * 10L;
    private long mSize_Count = 0;
    private TextView textUplC;

    //调用应用
    private String mPackageName = "com.sf.sweagent"; //顺丰应用包名

    //拍照相关
    private int matterCount = 1;
    private boolean mDestroyed = false;
    private Camera camera;//声明相机
    private String filepath = "";//照片保存路径
    private TextView textView;
    private final  int PHOTO_NUM = 100;
    private int mPhoto_count = 0;
    private final int PREVIEW_TIME = 2000;
    private final int PHOTO_TIME = 1000;
    private CameraPreview backPreview;
    private FrameLayout frameLayout;
    private TextView textPhoC;
    private String jpgPath = "/sdcard/IWatchTest/photo/";


    //振动相关
    private long[] mPattern = {0,500};
    private final int VIBRATION_NUM = 150;
    private Vibrator mVibrator;
    private int mVibration_count = 0;
    private TextView textVibC;


    //定位信息
    private String locateType;
    private LocationManager locationManager;
    private double mLongitude = 0.0;
    private double mLatitude = 0.0;
    private double mAltitude = 0.0;
    private boolean ifFirst = true;

    //文件信息
    private String mPath = "/sdcard/IWatchTest/";
    private String mFileName = "Battery.txt";


    //电量与时间信息
    static final int UPDATE_INTERVAL = 1000*60;
    private int mPercent = 0;
    private long mInitTime = 0L;
    private long mSleepTime = 1000 * 60L * 60 * 8 ;// * 60 * 8L;

    //休眠管理
    DevicePolicyManager mPolicyManager = null;
    private ComponentName componentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //文件相关创建
        FileUtil.createDir(jpgPath);
//        FileUtil.createDir(mPath);
//        FileUtil.createFile(mFileName);

        //控件相关
        textPhoC = (TextView) findViewById(R.id.text_photo_count);
        textDowC = (TextView) findViewById(R.id.text_download_count);
        textUplC = (TextView) findViewById(R.id.text_upload_count);
        textVibC = (TextView) findViewById(R.id.text_vibration_count);
        backPreview = new CameraPreview(this, 1);
        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(backPreview);
        textView = (TextView) findViewById(R.id.Text_View);
        textView.getBackground().setAlpha(0);

        // 灭屏权限
//        componentName = new ComponentName(this, MyAdmin.class);
//        mPolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
//        final boolean isAdminActive = mPolicyManager.isAdminActive(componentName);

        startService(new Intent(this,GPSService.class));
        //GPS相关
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        locateType = locationManager.GPS_PROVIDER; //定位类型：GPS
//        setLocationManager();

        //振动相关
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //下载相关
        registerReceiver(DownloadReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//        //电量监控
//        registerReceiver(mBatteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));





        mHandler = new Handler();

        mHandler.post(run2);




    }





    Runnable run2 = new Runnable() {
        @Override
        public void run() {

            Log.d(Constants.TAG, Thread.currentThread().getName());
            switch (matterCount){
                case 1:
                    if (mDestroyed) break;
                    frameLayout.removeAllViews();
                    backPreview = null;
                    textView.getBackground().setAlpha(255);//关闭显示
                    backPreview = new CameraPreview(MainActivity.this,0);
                    frameLayout.addView(backPreview);

                    matterCount = 2;
                    Log.d(Constants.TAG, "切换到后摄像头");
                    mPhoto_count++;
                    textPhoC.setText("" + mPhoto_count);
                    mHandler.postDelayed(this,PREVIEW_TIME);
                    break;
                case 2:
                    if (mDestroyed) break;


                    camera = backPreview.getCamera();
                    Log.d(Constants.TAG,"if camera null "+(camera == null));

                    camera.autoFocus(new Camera.AutoFocusCallback() {//自动对焦
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            // 1TODO Auto-generated method stub
                            if(success)
                            {
                                //设置参数，并拍照
                                Camera.Parameters params = camera.getParameters();
                                params.setPictureFormat(PixelFormat.JPEG);//图片格式
                                params.setPreviewSize(800, 480);//图片大小
                                camera.setParameters(params);//将参数设置到我的camera
                                camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
                            }

                        }
                    });


                    //mUIHandler.sendEmptyMessage(CASE_TAKEPHOTO);
                    matterCount = 1;
                    if (mPhoto_count == PHOTO_NUM){
                        vibrationProcess();
                    }else {
                        mHandler.postDelayed(this,PHOTO_TIME);
                    }
                    break;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
        mVibrator.cancel();
        unregisterReceiver(DownloadReceiver);
    }

//    private void uploadProcess(){
//        Toast.makeText(this,"开始上传",Toast.LENGTH_SHORT).show();
//        mHandler.post(new Runnable() {
//            @Override
//            public void run()
//            {
//
//                StringBuilder sb = new StringBuilder("");
//                for (int i=0; i<100; i++){
//                    sb.append("1234567890");
//                }
//                if (mUploadFlag == 1)
//                    uploadFile(sb.toString());
//                if (mPassFlag == 1 && mFailFlag ==0){
//                    mSize_Count += 1000;
//                    Log.d(Constants.TAG,"上传成功");
//                    mUIHandler.sendEmptyMessage(CASE_UPLOAD);
//                }
////                else{
////                    Log.d(Constants.TAG,"上传失败");
////                }
//
//
//                //if (mSize_Count >= DATA_SIZE){
//                 //   vibrationProcess();
//                //}else{
//                    if (!mDestroyed)
//                    mHandler.postDelayed(this, 10);
//                //}
//            }
//        });
//    }

    private void downloadProcess(){
        Toast.makeText(this,"开始下载",Toast.LENGTH_SHORT).show();
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        textDowC.setText("正在下载");

        String apkUrl = "http://download.zhushou.sogou.com/zhushou/baidu/SogouMobileTools_baidu_tuiguang37.apk";


        DownloadManager.Request request = new

                DownloadManager.Request(Uri.parse(apkUrl));
        request.setDestinationInExternalPublicDir("", "/MyFavorite/test.zip");

        mDownLoadId = downloadManager.enqueue(request);
        //openApp(mPackageName);
    }

    private void vibrationProcess(){
        Toast.makeText(this,"开始振动",Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run()
            {
                mVibrator.vibrate(mPattern,-1);
                mVibration_count++;
                //mUIHandler.sendEmptyMessage(CASE_VIBRATION);
                textVibC.setText("" + mVibration_count);
                if (mVibration_count >= VIBRATION_NUM){
                    downloadProcess();

                }else{
                    if (!mDestroyed)
                        mHandler.postDelayed(this, 500);
                }
            }
        });
    }



    //创建jpeg图片回调数据对象
    Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 1TODO Auto-generated method stub
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                //自定义文件保存路径  以拍摄时间区分命名
                filepath = "/sdcard/IWatchTest/photo/"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".jpg";
                File file = new File(filepath);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩的流里面
                bos.flush();// 刷新此缓冲区的输出流
                bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
                camera.stopPreview();//关闭预览 处理数据
                camera.startPreview();//数据处理完后继续开始预览
                bitmap.recycle();//回收bitmap空间
            } catch (Exception e) {
                // 1TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };




    private  BroadcastReceiver  DownloadReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onReceive(Context context, Intent intent) {
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Log.d(Constants.TAG, "下载的IDonReceive: "+completeDownloadId);


            if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
               if (completeDownloadId == mDownLoadId){
                   mIfDownloadComplete = true;
                   textDowC.setText("下载完成");
                   openApp(mPackageName);
               }
            }
        }
    };


    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int current = intent.getExtras().getInt("level");// 获得当前电量
            int total = intent.getExtras().getInt("scale");// 获得总电量
            mPercent = current * 100 / total;
        }
    };

    private void openApp(String packageName) {

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        intent.putExtra("type", true);
        startActivity(intent);
    }



    private void uploadFile(String m)//上传服务函数
    {
        //String end = "\r\n";
        //String twoHyphens = "--";
        //String boundary = "*****";
        String param = "action=insert&data=" + m;
        mUploadFlag = 0;
        try
        {
            URL url = new URL(mActionUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            //设置参数
            httpConn.setDoOutput(true);   //需要输出
            httpConn.setDoInput(true);   //需要输入
            httpConn.setUseCaches(false);  //不允许缓存
            httpConn.setRequestMethod("POST");   //设置POST方式连接
            //设置请求属性
            httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpConn.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            httpConn.setRequestProperty("Charset", "UTF-8");
            //连接,也可以不用明文connect，使用下面的httpConn.getOutputStream()会自动connect

            httpConn.connect();
            Log.d(Constants.TAG,"连接结束");
            //建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpConn.getOutputStream());

            dos.writeBytes(param);
            dos.flush();
            dos.close();
            //获得响应状态
            int resultCode = httpConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == resultCode)
            {
                //b是服务器的返回信息
                StringBuffer b = new StringBuffer();
                String readLine = new String();
                InputStream in = httpConn.getInputStream();//服务器返回流
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                while ((readLine = responseReader.readLine()) != null)//readLine()一行一行的读取，如果读到结尾返回null
                {
                    b.append(readLine).append("\n");
                }
                responseReader.close();
                //PassResult="上传成功" + b.toString().trim() + param;

                mPassFlag = 1;
                mFailFlag = 0;
            }
            else
            {
                Log.d(Constants.TAG,"上传失败");
                mFailFlag = 1;
                mPassFlag = 0;
            }

        }
        catch (Exception e)
        {
            Log.d(Constants.TAG,"上传异常");
            mFailFlag = 1;
            mPassFlag = 0;
        }
        mUploadFlag= 1;//测试完成将完成标志位置1
    }




    static class UIHandler extends Handler {
        private SoftReference<MainActivity> mainActivitySoftReference;

        public UIHandler(MainActivity mainActivity) {
            this.mainActivitySoftReference = new SoftReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mainActivitySoftReference.get();
            if (mainActivity == null) return;

            switch (msg.what) {
                case CASE_TAKEPHOTO:
                    mainActivity.textPhoC.setText(" " + mainActivity.mPhoto_count);
                    break;
                case CASE_DOWNLOAD:
                    mainActivity.textDowC.setText("正在下载中...");
                    break;
                case CASE_VIBRATION:
                    mainActivity.textVibC.setText(" " + mainActivity.mVibration_count);
                    break;
                case CASE_UPLOAD:
                    mainActivity.textUplC.setText("已上传: " + mainActivity.mSize_Count * 0.1 / 1024 /1024 + "MB");

                    break;
            }

        }


    }


    private void setLocationManager() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "GPS权限未开启！" );
            Toast.makeText(MainActivity.this, "GPS权限未开启!", Toast.LENGTH_SHORT).show();
            return;
        }
        //Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000*60, 0, locationListener);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60, 0, locationListener);
    }

    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(Constants.TAG, "GPS权限未开启！" );
            Toast.makeText(MainActivity.this, "GPS权限未开启!", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
        if (location != null){
            mLongitude = location.getLongitude();
            mLatitude = location.getLatitude();
            mAltitude = location.getAltitude();
            Log.i(Constants.TAG, "时间：" + location.getTime());
            Log.i(Constants.TAG, "经度：" + location.getLongitude());
            Log.i(Constants.TAG, "纬度：" + location.getLatitude());
            Log.i(Constants.TAG, "海拔：" + location.getAltitude());
        }else {
            Toast.makeText(MainActivity.this, "GPS未能获取", Toast.LENGTH_SHORT).show();
        }


    }


    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            Toast.makeText(MainActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
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




}
