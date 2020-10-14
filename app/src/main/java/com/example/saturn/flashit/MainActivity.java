package com.example.saturn.flashit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends Activity {

    //private ImageButton button;
    @BindView(R.id.textView_progress)
    TextView textView;
    @BindView(R.id.imageToggleButton)
    ToggleButton button;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    private static final String Value_ZERO = "0";
    private boolean isFlashOn = false;
    private int freq;
    private Thread t;
    private StroboRunner stroboRunner;
    private boolean stopFlicker = false;
    public Context mContext;

    private CameraManager mCameraManager;
    private String cameraId;
    private final Time timeVar = new Time();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        ButterKnife.bind(this);

        checkForResources();
        seekBarMethod();
    }


    private void checkForResources() {
        PackageManager pm = mContext.getPackageManager();

        /*Checking availability of required camera hardware*/
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.NO_CAMERA_ERROR),
                    Toast.LENGTH_SHORT).show();
        } else {
//            initialiseResources();
            initialiseResourcesCamera2();
            setToggleButtonBehaviour();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initialiseResourcesCamera2() {

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        mCameraManager.registerTorchCallback(torchCallback, null);// (callback, handler)
        try {
            cameraId =  mCameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

//        checkForCameraPermission();
    }


    private void setFlashOn(Boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mCameraManager.setTorchMode(cameraId,enable);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /*CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            *//*isFlashOn = enabled;
            button.setChecked(enabled);*//*
        }
    };*/

    private void setToggleButtonBehaviour() {

        //Using toggle button with background image
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isFlashOn = isChecked;
                if (!isChecked) {
                    // The toggle is enabled
                    Log.i("info", "torch is turned off!");
                    setFlashOn(false);
                    isFlashOn = false;
                    seekBar.setProgress(0);
                    textView.setText(Value_ZERO);
                    if (t!= null) {
                        stopFlicker = true;
                        t = null;
                    }
                } else {
                    // The toggle is disabled
                    Log.i("info", "torch is turned on!");
                    setFlashOn(true);
                    isFlashOn = true;
                    stopFlicker = false;
                }
            }
        });
    }

    private void seekBarMethod() {
         /*SeekBar which will indicate value of brightness of torch*/
        seekBar.setMax(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                textView.setText(progressValue + "");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText(progressValue + "");
                flashFlicker();
            }
        });
    }

    private void flashFlicker() {
        if (isFlashOn) {
            freq = seekBar.getProgress();
            timeVar.setSleepTime(freq);
            stroboRunner = new StroboRunner();
            t = new Thread(stroboRunner);
            t.start();
            return;
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.SWICH_FLASH_ON), Toast.LENGTH_SHORT).show();
            seekBar.setProgress(0);
            textView.setText(Value_ZERO);
        }
    }

    private class StroboRunner implements Runnable {

        public void run() {
            try {
                while (!stopFlicker) {

                    if (freq != 0) {
                        setFlashOn(true);
                        Thread.sleep(timeVar.getSleepTime());
                        setFlashOn(false);
                        Thread.sleep(timeVar.getSleepTime());
                    } else {
                        setFlashOn(true);
                    }
                }

            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    /*Releasing camera resources*/
    private void releaseCamera(){
        setFlashOn(false);
        seekBar.setProgress(0);
        isFlashOn=false;
        textView.setText(Value_ZERO);
        button.setChecked(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
//        mCameraManager.unregisterTorchCallback(torchCallback);

    }


}
