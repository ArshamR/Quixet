package com.arsham.quixet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.SeekBar;


public class MainActivity extends Activity {
        protected static SeekBar brightbar;
        private SeekBar volumeBar;
        private SeekBar musicBar;

        private CheckBox checkbox;

        private AudioManager audioM;

        private int brightness;
        private int ringVol;
        private int musicVol;

        private float perc;

        private ContentResolver cResolver;

        private Window window;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            brightbar = (SeekBar) findViewById(R.id.brightbar);
            volumeBar = (SeekBar) findViewById(R.id.volumeBar);
            musicBar = (SeekBar) findViewById(R.id.musicBar);

            checkbox = (CheckBox) findViewById(R.id.checkBox);

            cResolver = getContentResolver();
            window = getWindow();
            brightbar.setMax(255);

            brightbar.setSecondaryProgress(255);
            volumeBar.setSecondaryProgress(255);
            musicBar.setSecondaryProgress(255);



        audioM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            musicBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeBar.setMax(audioM.getStreamMaxVolume(AudioManager.STREAM_RING));

            try{
                if(Settings.System.getInt( cResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                    checkbox.setChecked(true);
                }
                else{
                    checkbox.setChecked(false);
                }
            }catch (Exception e){
                Log.e("Error","Checkbox");
            }


            try {
                brightness = getBrightness();
                ringVol =  getRingVol();
                musicVol = getMusicVol();
            } catch (Exception e) {
                Log.e("Error", "cannot access system brightness.");
            }

            try {
                setBrightbar(brightness);
                setRingBar(ringVol);
                setMusicBar(musicVol);

            }catch (Exception e ) {
                Log.e("Error", "cannot set progress");
            }

            perc = (brightbar.getProgress() /(float)255) * 100;

            brightbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                  updateBrightness();
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub

                    Settings.System.putInt(cResolver,
                            Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    try {
                        if (checkbox.isChecked()) {
                            checkbox.toggle();
                        }
                    }catch (Exception e){
                        Log.e("Error","Toggle checkbox");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    //Set the minimal brightness level
                    if (progress <= 20) {
                        brightness = 20;
                    }
                    else{
                        brightness = progress;
                    }

                    //Calculate the brightness percentage
                    perc = (progress / (float) 255) * 100;

                }
            });

            try {
                musicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        audioM.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }catch(Exception e){
                Log.e("Error", "Music Bar");
            }

            try {
                volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        audioM.setStreamVolume(AudioManager.STREAM_RING, progress, 0);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }catch(Exception e){
                Log.e("Error","Volume bar");
            }
        }



    public void onCheckBoxClicked(View v){
        CheckBox checkBox = (CheckBox)v;
        if(checkBox.isChecked()){
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            refreshScreen(-1);
        }
        else{
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            refreshScreen(getBrightness());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(Settings.System.getInt(cResolver,Settings.System.SCREEN_BRIGHTNESS_MODE)== 1){
                refreshScreen(-1);
                    checkbox.setChecked(true);
            }
            else{
                refreshScreen(getBrightness());
                checkbox.setChecked(false);
            }
            ringVol =  getRingVol();
            musicVol = getMusicVol();

        }catch(Exception e ){
            Log.e("Error", "Cannot access system brightness on resume");
        }

        setBrightbar(brightness);
        setRingBar(ringVol);
        setMusicBar(musicVol);
    }

    protected  void onPause(){
        super.onPause();
        startService(new Intent(this, ChatHeadService.class));
        finish();
    }

    public int getBrightness(){
        try {
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        }catch (Exception e){
            Log.e("Error", "Cannot get brightness");
        }
        return brightness;
    }

    public int getRingVol(){
        try {
            ringVol =  audioM.getStreamVolume(AudioManager.STREAM_RING);
        }catch (Exception e){
            Log.e("Error", "Cannot get ring volumme");
        }
        return ringVol;
    }

    public int getMusicVol(){
        try {
            musicVol = audioM.getStreamVolume(AudioManager.STREAM_MUSIC);
        }catch (Exception e){
            Log.e("Error", "Cannot get music volume");
        }
        return musicVol;
    }

    public void setBrightbar(int bright){
        brightbar.setProgress(bright);
    }
    public void setRingBar(int ringV){
        volumeBar.setProgress(ringV);
    }
    public void setMusicBar(int musicV){
        musicBar.setProgress(musicV);
    }

    public void updateBrightness(){
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
            WindowManager.LayoutParams layoutpars = window.getAttributes();
            layoutpars.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            layoutpars.screenBrightness = perc / 100;
            window.setAttributes(layoutpars);
        }catch (Exception e){
            Log.e("Error", "Update Brightness");
        }
    }

    public void refreshScreen(float brightness){
        WindowManager.LayoutParams layoutP = window.getAttributes();
        if(brightness < 0){
            layoutP.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        }
        else{
            layoutP.screenBrightness = brightness;
        }
        window.setAttributes(layoutP);
    }
}
