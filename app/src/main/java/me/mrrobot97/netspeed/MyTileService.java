package me.mrrobot97.netspeed;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;

/**
 * Created by mrrobot on 16/12/31.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class MyTileService extends TileService {
    @Override
    public void onClick() {
        super.onClick();
        if(WindowUtil.isShowing){
            stopService(new Intent(this,SpeedCalculationService.class));
        }else{
            startService(new Intent(this,SpeedCalculationService.class));
        }

    }

    
}
