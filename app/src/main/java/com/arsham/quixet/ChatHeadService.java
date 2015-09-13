package com.arsham.quixet;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by arsha_000 on 8/10/2015.
 */
public class ChatHeadService  extends Service {

    private static final float CANCEL_PARAM_VERTICAL_MARGIN = .1f;

    private WindowManager windowManager;
    private ImageView chatHead;
    private ImageView cancel;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams cancelParams;
    private boolean moving = false;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        Log.d("On Create ", "Service");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = new ImageView(this);
        cancel = new ImageView(this);

        chatHead.setImageResource(R.drawable.gear);
        cancel.setImageResource(R.drawable.ic_cancel);

        params = setParams();
        cancelParams = setParams();

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        cancelParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        cancelParams.verticalMargin = CANCEL_PARAM_VERTICAL_MARGIN;
    }

    private WindowManager.LayoutParams setParams(){
        WindowManager.LayoutParams parm;
        try {
            parm = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            return parm;
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    public int onStartCommand(Intent intent, int flags, int startid) {
        try {
            windowManager.addView(chatHead, params);
        }catch (Exception e){
            Log.d("Image View","Image view already created");
        }
        Log.d("On Start Command", "Service");

        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private final int BUTTTON_PRESS_DIFF = 15;
            private final int REMOVE_IMAGEVIEWS_DIFF = 60;

            private int[] cancelLoc = new int[2];
            private int[] buttonLoc = new int[2];

            private boolean viewAdded = false;


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int xDiff = params.x - initialX;
                        int yDiff = params.y - initialY;

                        if(xDiff < BUTTTON_PRESS_DIFF
                                && yDiff < BUTTTON_PRESS_DIFF
                                && xDiff > -BUTTTON_PRESS_DIFF
                                && yDiff > -BUTTTON_PRESS_DIFF)
                        {
                            try {
                                windowManager.removeView(chatHead);
                                windowManager.removeView(cancel);
                            }catch(Exception e){
                                Log.d("Image views","Already removed");
                            }
                            chatHead.performClick();
                        }

                        cancel.getLocationOnScreen(cancelLoc);
                        chatHead.getLocationOnScreen(buttonLoc);

                        int xDiff2;
                        int yDiff2;

                        xDiff2 = buttonLoc[0] - cancelLoc[0];
                        yDiff2 = buttonLoc[1] - cancelLoc[1];

                         if(xDiff2 < REMOVE_IMAGEVIEWS_DIFF &&
                                 xDiff2 > -REMOVE_IMAGEVIEWS_DIFF &&
                                 yDiff2 < REMOVE_IMAGEVIEWS_DIFF &&
                                 yDiff2 > -REMOVE_IMAGEVIEWS_DIFF)
                         {
                            try {
                                stopSelf();
                            }catch (Exception e){
                                Log.d("Trying to remove", "Failed");
                            }
                        }

                        try {
                            windowManager.removeView(cancel);
                            viewAdded = false;
                        }catch(Exception e){
                            Log.d("Cancel image", "Already removed");
                    }
                        moving = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        moving = true;
                        try{
                            if(!viewAdded) {
                                if (moving) {
                                    windowManager.addView(cancel, cancelParams);
                                    viewAdded = true;
                                }
                            }
                        }catch (Exception e){
                            Log.d("ImageView Cancel", "Error");
                        }
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });

        chatHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button", "Clicked");
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        return START_STICKY;
    }

    public void makeToast(float x, float y){
        String xVal = "X  " + x;
        String yVal = "\nY  :" + y;
        Toast toast = Toast.makeText(getApplicationContext(),xVal + yVal,Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
        stopSelf();
    }
}



