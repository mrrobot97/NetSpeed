---
layout: post
author: mrrobot97
tags: Android 悬浮窗
title: 制作一个网速显示悬浮窗
---
>2016转眼就要过去了，刚刚参加完学院举办的元旦晚会，看了看系里的大牛的各种事迹，内心感慨万分。回来继续安心做我的小码农，顺便更一下将近一个月没有更新的博客。

这次带来的是一个悬浮窗网速显示计，先看下效果：

![demo](https://blog-1256554550.cos.ap-beijing.myqcloud.com/demo2.gif)

这里主要是在桌面上显示一个悬浮窗，利用了WindowManager以及Service,接下来看看如何实现这样一个效果：
首先APP必须获得在桌面上显示悬浮窗的机会，很多第三方ROM都限制了这一权限，我们首先就是要申请改权限，代码如下：

```java

public boolean checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        }
        return true;
    }

```

该函数首先检查APP是否有显示悬浮窗的权限，如果没有，就跳转到该APP设置悬浮窗权限的界面，如下图所示：

![](https://blog-1256554550.cos.ap-beijing.myqcloud.com/662ABA6C-D700-4CEC-A883-18714BCDBCEB.png)

然后先编写我们的悬浮窗，布局很简单，就是两个TextView：

```java

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#88000000">


    <TextView
        android:layout_marginLeft="3dp"
        android:layout_marginRight="3dp"
        android:id="@+id/speed_up"
        android:text="upload speed"
        android:gravity="left"
        android:textSize="10dp"
        android:layout_centerHorizontal="true"
        android:textColor="@android:color/white"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <TextView
        android:layout_marginLeft="3dp"
        android:layout_marginRight="3dp"
        android:id="@+id/speed_down"
        android:layout_below="@id/speed_up"
        android:text="download speed"
        android:gravity="left"
        android:textSize="10dp"
        android:layout_centerHorizontal="true"
        android:textColor="@android:color/white"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</RelativeLayout>
```

SpeedView:

```java

public class SpeedView extends FrameLayout {
    private Context mContext;
    public TextView downText;
    public TextView upText;
    private WindowManager windowManager;
    private int statusBarHeight;
    private float preX,preY,x,y;

    public SpeedView(Context context) {
        super(context);
        mContext=context;
        init();
    }

    private void init() {
        statusBarHeight=WindowUtil.statusBarHeight;
        windowManager= (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        //a view inflate itself, that's funny
        inflate(mContext,R.layout.speed_layout,this);
        downText= (TextView) findViewById(R.id.speed_down);
        upText= (TextView) findViewById(R.id.speed_up);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preX=event.getRawX();preY=event.getRawY()-statusBarHeight;
                return true;
            case MotionEvent.ACTION_MOVE:
                x=event.getRawX();y=event.getRawY()-statusBarHeight;
                WindowManager.LayoutParams params= (WindowManager.LayoutParams) getLayoutParams();
                params.x+=x-preX;
                params.y+=y-preY;
                windowManager.updateViewLayout(this,params);
                preX=x;preY=y;
                return true;
            default:
                break;

        }
        return super.onTouchEvent(event);
    }


```
在SpeedView里我们重写了onTouchEvent事件，这样就能响应我们的拖拽事件了，注意这里更新SpeedView的位置是通过改变WindowManager.LayoutParam的x和y来实现的，调用`windowManager.updateViewLayout(this,params)`来更新位置。

因为我们的网速显示悬浮窗要脱离于Activity的生命周期而独立存在，因此需要通过Service来实现：

```java

public class SpeedCalculationService extends Service {
    private WindowUtil windowUtil;
    private boolean changed=false;

    @Override
    public void onCreate() {
        super.onCreate();
        WindowUtil.initX= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_X,0);
        WindowUtil.initY= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_Y,0);
        windowUtil=new WindowUtil(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        changed=intent.getBooleanExtra(MainActivity.CHANGED,false);
        if(changed){
            windowUtil.onSettingChanged();
        }else{
            if(!windowUtil.isShowing()){
                windowUtil.showSpeedView();
            }
            SharedPreferencesUtils.putToSpfs(this,MainActivity.IS_SHOWN,true);
        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowManager.LayoutParams params= (WindowManager.LayoutParams) windowUtil.speedView.getLayoutParams();
        SharedPreferencesUtils.putToSpfs(this, INIT_X,params.x);
        SharedPreferencesUtils.putToSpfs(this, INIT_Y,params.y);
        if(windowUtil.isShowing()){
            windowUtil.closeSpeedView();
            SharedPreferencesUtils.putToSpfs(this,MainActivity.IS_SHOWN,false);
        }
        Log.d("yjw","service destroy");
    }
}
```

这里的WindowUtil其实就是一个工具类，帮助我们控制悬浮窗SpeedView的显示与隐藏：

```java

public class WindowUtil {
    public static int statusBarHeight=0;
    //记录悬浮窗的位置
    public static int initX,initY;
    private WindowManager windowManager;
    public SpeedView speedView;
    private WindowManager.LayoutParams params;
    private Context context;

    public boolean isShowing() {
        return isShowing;
    }

    private boolean isShowing=false;

    public static final int INTERVAL=2000;
    private long preRxBytes,preSeBytes;
    private long rxBytes,seBytes;
    private Handler handler=new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            calculateNetSpeed();
            sendEmptyMessageDelayed(0,INTERVAL);
        }
    };

    public void onSettingChanged(){
        String setting= (String) SharedPreferencesUtils.getFromSpfs(context,MainActivity.SETTING,MainActivity.BOTH);
        if(setting.equals(MainActivity.BOTH)){
            speedView.upText.setVisibility(View.VISIBLE);
            speedView.downText.setVisibility(View.VISIBLE);
        }else if(setting.equals(MainActivity.UP)){
            speedView.upText.setVisibility(View.VISIBLE);
            speedView.downText.setVisibility(View.GONE);
        }else{
            speedView.upText.setVisibility(View.GONE);
            speedView.downText.setVisibility(View.VISIBLE);
        }
    }

    private void calculateNetSpeed() {
        rxBytes=TrafficStats.getTotalRxBytes();
        seBytes=TrafficStats.getTotalTxBytes()-rxBytes;
        double downloadSpeed=(rxBytes-preRxBytes)/2;
        double uploadSpeed=(seBytes-preSeBytes)/2;
        preRxBytes=rxBytes;
        preSeBytes=seBytes;
        //根据范围决定显示单位
        String upSpeed=null;
        String downSpeed=null;

        NumberFormat df= java.text.NumberFormat.getNumberInstance();
        df.setMaximumFractionDigits(2);

        if(downloadSpeed>1024*1024){
            downloadSpeed/=(1024*1024);
            downSpeed=df.format(downloadSpeed)+"MB/s";
        }else if(downloadSpeed>1024){
            downloadSpeed/=(1024);
            downSpeed=df.format(downloadSpeed)+"B/s";
        }else{
            downSpeed=df.format(downloadSpeed)+"B/s";
        }

        if(uploadSpeed>1024*1024){
            uploadSpeed/=(1024*1024);
            upSpeed=df.format(uploadSpeed)+"MB/s";
        }else if(uploadSpeed>1024){
            uploadSpeed/=(1024);
            upSpeed=df.format(uploadSpeed)+"kB/s";
        }else{
            upSpeed=df.format(uploadSpeed)+"B/s";
        }

        updateSpeed("↓ "+downSpeed,"↑ "+upSpeed);
    }

    public WindowUtil(Context context) {
        this.context = context;
        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        speedView=new SpeedView(context);
        params=new WindowManager.LayoutParams();
        params=new WindowManager.LayoutParams();
        params.x=initX;
        params.y=initY;
        params.width=params.height=WindowManager.LayoutParams.WRAP_CONTENT;
        params.type=WindowManager.LayoutParams.TYPE_PHONE;
        params.gravity= Gravity.LEFT|Gravity.TOP;
        params.format= PixelFormat.RGBA_8888;
        params.flags=WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                //设置悬浮窗可以拖拽至状态栏的位置
//        | WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

    }

    public void showSpeedView(){
        windowManager.addView(speedView,params);
        isShowing=true;
        preRxBytes= TrafficStats.getTotalRxBytes();
        preSeBytes=TrafficStats.getTotalTxBytes()-preRxBytes;
        handler.sendEmptyMessage(0);
    }

    public void closeSpeedView(){
        windowManager.removeView(speedView);
        isShowing=false;
    }

    public void updateSpeed(String downSpeed,String upSpeed){
        speedView.upText.setText(upSpeed);
        speedView.downText.setText(downSpeed);
    }
}

```java

WindowUtil类中也包含了一个很重要的方法，那就是计算网速。这里计算网速的方法很简单，Android提供了一个类`TrafficStats`,这个类里面为我们提供了好多接口，我们用到了其中的两个:

	1.public static long getTotalTxBytes ()
	Return number of bytes transmitted since device boot. Counts packets across all network interfaces, and always increases monotonically since device boot. Statistics are measured at the network layer, so they include both TCP and UDP usage.
	2.public static long getTotalRxPackets ()
	Return number of packets received since device boot. Counts packets across all network interfaces, and always increases monotonically since device boot. Statistics are measured at the network layer, so they include both TCP and UDP usage.


可以看出，getTotalTxBytes()方法返回系统自开机到现在为止所一共传输的数据的字节数，包括上传的数据和下载的数据；而getTotalRxPackets()方法返回的是系统自开机到现在为止所一共接收到也就是下载的数据的字节数，用getTotalTxBytes()-getTotalRxPackets()自然就是系统开机到现在为止所上传的数据的字节数。

这样每隔一定时间，我们计算一下系统自开机到目前所接受的数据包的字节数和所发送的数据的字节数的变化量，用变化量除以时间，就是这段时间的平均网速了。

为了每个一段时间计算一下网速，我们利用了一个Handler来实现这个定时任务

```java

private Handler handler=new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            calculateNetSpeed();
            sendEmptyMessageDelayed(0,INTERVAL);
        }
    };
```

这里要注意将SpeedView添加到屏幕上，也就是添加到WindowManager里的时候，这个WindowManager.LayoutParams十分重要，其参数都是有用的，这里就不细讲了,详细介绍请移步[官方文档](https://developer.android.com/reference/android/view/WindowManager.LayoutParams.html).

最后要将我们的悬浮窗设置为开机自启动的，利用一个BroadcastReceiver就可以了：

```java

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("yjw","receiver receive boot broadcast");
        boolean isShown= (boolean) SharedPreferencesUtils.getFromSpfs(context,MainActivity.IS_SHOWN,false);
        if(isShown){
            context.startService(new Intent(context,SpeedCalculationService.class));
        }
    }
}
```
在Manifest里这样注册我们的BroadcastReceiver：

```java

 <receiver android:name="me.mrrobot97.netspeed.MyBroadcastReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <category android:name="android.intent.category.HOME" />
            </intent-filter>
        </receiver>
```

这样当系统启动完成，就会开启我们的Service。注意这里<intent-filter>中的<category>不可省略，亲测省略后BroadcastReceiver无法接收到系统广播。

最后还有一点，在Manifest的MainActivity条目中加一个属性：`android:excludeFromRecents="true"`

这样我们的ManiActivity就不会显示在最近任务列表，防止用户清空任务列表时将我们的Sercvice进程终结了。

完整的项目地址[Github](https://github.com/mrrobot97/NetSpeed)
	
	
