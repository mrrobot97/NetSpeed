package me.mrrobot97.netspeed;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import static me.mrrobot97.netspeed.MainActivity.INIT_X;
import static me.mrrobot97.netspeed.MainActivity.INIT_Y;

/**
 * Created by mrrobot on 16/12/28.
 */

public class SpeedCalculationService extends Service {
    private WindowUtil windowUtil;
    private boolean changed=false;
    private final int FOREGROUND_ID=1997;

    @Override
    public void onCreate() {
        super.onCreate();
        WindowUtil.initX= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_X,0);
        WindowUtil.initY= (int) SharedPreferencesUtils.getFromSpfs(this,INIT_Y,0);
        windowUtil=new WindowUtil(this);
//        Intent intent=new Intent(this,MainActivity.class);
//        PendingIntent pi=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder=new Notification.Builder(this);
//        Notification notification=builder.setContentIntent(pi).build();
//        startForeground(FOREGROUND_ID,notification);
//        FakeService fakeService=new FakeService();
//        fakeService.startForeground(FOREGROUND_ID,notification);
//        fakeService.stopSelf();
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
