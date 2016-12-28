package me.mrrobot97.netspeed;

import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by mrrobot on 16/12/28.
 */

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

}
