package me.mrrobot97.netspeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by mrrobot on 16/12/28.
 */

//开机自启动
public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("yjw","receiver receive boot boradcast");
        boolean isShown= (boolean) SharedPreferencesUtils.getFromSpfs(context,MainActivity.IS_SHOWN,false);
        if(isShown){
            context.startService(new Intent(context,SpeedCalculationService.class));
        }
    }
}
