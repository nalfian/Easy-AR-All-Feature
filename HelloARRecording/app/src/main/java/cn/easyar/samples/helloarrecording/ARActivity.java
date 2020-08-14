//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarrecording;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.easyar.CameraDevice;
import cn.easyar.FunctorOfVoidFromPermissionStatusAndString;
import cn.easyar.FunctorOfVoidFromRecordStatusAndString;
import cn.easyar.PermissionStatus;
import cn.easyar.RecordStatus;
import cn.easyar.Engine;
import cn.easyar.Recorder;

import android.app.Activity;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARRecording
    *      Package Name: cn.easyar.samples.helloarrecording
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "RXytJkFvtTpZCUsIAYFLk0nYVZ7vZf+Mb1GFXXVOmw1BXp0QdVOKXTofnxNmVJ8RQFyPFi5ekVFpWdxTIlCfDHRYjDRlRLcbIgfPUyJRlxxlU40acx/EJHsfnApuWZIaSVmNXTpmo1MiS58NaVyQC3MfxCQiXpESbUiQFnRE3CIsH44TYUmYEHJQjV06ZtwIaVOaEHdO3FMiUJ8cImDSXW1SmgpsWI1dOmbcDGVTjRoudJMeZ1iqDWFelRZuWtxTIk6bEXNY0DxsUosbUlidEGdTlwtpUpBdLB+NGm5Om1FSWJ0QclmXEWcf0l1zWJAMZROxHWpYnQtUT58ca1SQGCIR3AxlU40aLm6LDWZcnRpUT58ca1SQGCIR3AxlU40aLm6OHnJOmyxwXIoWYVGzHnAf0l1zWJAMZROzEHRUkRFUT58ca1SQGCIR3AxlU40aLnmbEXNYrQ9hSZcebHCfDyIR3AxlU40aLn6/O1RPnxxrVJAYImDSXWVFjhZyWKoWbVitC2FQjl06U4sTbBHcFnNxkRxhUdxFZlySDGVA0gQiX4sRZFGbNmRO3EVbH50RLlifDHlcjFFzXJMPbFiNUWhYkhNvXIwNZV6RDWRUkBgiYNJddlyMFmFTigwiB6VdY1KTEnVTlwt5H6NTIk2SHnRbkQ1tTtxFWx+fEWRPkRZkH6NTIlCRG3VRmwwiB6Vdc1iQDGUTtxJhWpsrclydFGlTmV0sH40abk6bUUNRkQpkb5scb1qQFnRUkREiEdwMZVONGi5vmxxvT5oWblrcUyJOmxFzWNAwYlebHHRpjB5jVpcRZx/SXXNYkAxlE60KclufHGVpjB5jVpcRZx/SXXNYkAxlE60PYU+NGlNNnwtpXJIyYU3cUyJOmxFzWNAyb0mXEG5pjB5jVpcRZx/SXXNYkAxlE7oabk6bLHBcihZhUbMecB/SXXNYkAxlE70+RGmMHmNWlxFnH6NTIliGD2lPmytpUJssdFyTDyIHkApsUdJdaU6yEGNckl06W58Tc1iDU3sfnApuWZIaSVmNXTpm3F1dEdwJYU+XHm5JjV06Ztwcb1CTCm5UigYiYNJdcFGfC2ZSjBJzH8QkIlSRDCJg0l1tUpoKbFiNXTpm3AxlU40aLnSTHmdYqg1hXpUWblrcUyJOmxFzWNA8bFKLG1JYnRBnU5cLaVKQXSwfjRpuTptRUlidEHJZlxFnH9Jdc1iQDGUTsR1qWJ0LVE+fHGtUkBgiEdwMZVONGi5uiw1mXJ0aVE+fHGtUkBgiEdwMZVONGi5ujh5yTpsscFyKFmFRsx5wH9Jdc1iQDGUTsxB0VJERVE+fHGtUkBgiEdwMZVONGi55mxFzWK0PYUmXHmxwnw8iEdwMZVONGi5+vztUT58ca1SQGCJg0l1lRY4WcliqFm1YrQthUI5dOlOLE2wR3BZzcZEcYVHcRWZckgxlQKMCGPYYwem5yeW+aJBKkHdVK+kYa12qxHH4ADw+Xu7LI8YVZhT8s3++zSKQMirB/oq+Jez4II6AGxusxoNjN+CM1tKUCw5fNqzZ8A4OstARVncRFhDeD3uI9VLklUbr4zkcjbBP3Mu9amDSNp+YLvl7oR/ImCfY0mFQbjMslFKI7sSr3+Ur4PSqHKmF3Z8znepJzdPskNKbgGDp2Zmi3O1kTjQtMbuVhiW1W1P/Rtc0be1T505ueqfnMt9Qw9xIvI55V7+pgRIsiXKk6GdcOpnaN7ZRN62VszcrTG60akJPfIf5hoxoMWRjQIQSJjGQQoXZ/jdOJOHZ5zSwdGwWAD3+fw==";
    private GLView glView;
    private String url = "";
    private boolean started = false;

    private String prepareUrl()
    {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String FileName = "EasyAR_Recording_" + timeStamp + ".mp4";
        File folder = new File("/sdcard/Movies");
        if (!folder.exists())
            folder.mkdirs();
        return "/sdcard/Movies/" + FileName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.");
            Toast.makeText(ARActivity.this, Engine.errorMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!CameraDevice.isAvailable()) {
            Toast.makeText(ARActivity.this, "CameraDevice not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!Recorder.isAvailable()) {
            Toast.makeText(ARActivity.this, "Recorder not available.", Toast.LENGTH_LONG).show();
            return;
        }

        glView = new GLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                final Button recordCapture  = (Button)findViewById(R.id.ib_capture );
                recordCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (started) {
                            started = false;
                            recordCapture.setText("CAPTURE");
                            glView.stopRecording();
                            Toast.makeText(ARActivity.this, "Recorded at " + url, Toast.LENGTH_LONG).show();
                            return;
                        }
                        glView.requestPermissions(new FunctorOfVoidFromPermissionStatusAndString() {
                            @Override
                            public void invoke(int status, String msg) {

                                switch (status)
                                {
                                    case PermissionStatus.Denied:
                                        started = false;
                                        recordCapture.setText("CAPTURE");
                                        Log.e("HelloAR", "Permission Denied" + msg);
                                        Toast.makeText(ARActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                        break;
                                    case PermissionStatus.Error:
                                        started = false;
                                        recordCapture.setText("CAPTURE");
                                        Log.e("HelloAR", "Permission Error" + msg);
                                        Toast.makeText(ARActivity.this, "Permission Error", Toast.LENGTH_SHORT).show();
                                        break;
                                    case PermissionStatus.Granted:
                                        Log.i("HelloAR", "Permission Granted");

                                        url = prepareUrl();

                                        glView.startRecording(url, new FunctorOfVoidFromRecordStatusAndString() {
                                            @Override
                                            public void invoke(int status, String value) {
                                                switch (status)
                                                {
                                                    case RecordStatus.OnStarted:
                                                        break;
                                                    case RecordStatus.OnStopped:
                                                        started = false;
                                                        recordCapture.setText("CAPTURE");
                                                        break;
                                                    case RecordStatus.FileFailed:
                                                        started = false;
                                                        recordCapture.setText("CAPTURE");
                                                        break;
                                                    case RecordStatus.FailedToStart:
                                                        started = false;
                                                        recordCapture.setText("CAPTURE");
                                                        Toast.makeText(ARActivity.this, value, Toast.LENGTH_LONG).show();
                                                        break;
                                                    default:
                                                        break;

                                                }
                                                Log.i("HelloAR", "Recorder Callback status: " + Integer.toString(status) + ", MSG: " + value);
                                            }
                                        });
                                        Toast.makeText(ARActivity.this, "Recording...", Toast.LENGTH_SHORT).show();
                                        break;
                                }

                            }
                        });
                        started = true;
                        recordCapture.setText("STOP");
                    }
                });
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private interface PermissionCallback
    {
        void onSuccess();
        void onFailure();
    }
    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;
    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!started) {
            return;
        }
        started = false;
        glView.stopRecording();
        Toast.makeText(ARActivity.this, "Recorded at " + url, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (glView != null) { glView.onResume(); }
    }

    @Override
    protected void onPause()
    {
        if (glView != null) { glView.onPause(); }
        super.onPause();
    }
}
