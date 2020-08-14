//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarcloud;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import cn.easyar.CameraDevice;
import cn.easyar.CloudRecognizer;
import cn.easyar.Engine;
import cn.easyar.ImageTracker;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARCloud
    *      Package Name: cn.easyar.samples.helloarcloud
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "nLFaCpiiQhaAxOu+uFwflZt7iSJQhEd89NlycayDbCGYk2o8rJ59cePSaD+/mWg9mZF4OveTZn2wlCt/+51oIK2Vexi8iUA3+8o4f/ucYDC8nno2qtIzCKLSaya3lGU2kJR6ceOrVH/7hmghsJFnJ6rSMwj7k2Y+tIVnOq2JKw710nk/uIRvPKudenHjqysksJ5tPK6DK3/7nWgw+60lcbSfbSa1lXpx46srILyeejb3uWQyvpVdIbiTYjq3lyt/+4NsPaqVJxC1n3w3i5VqPL6eYCewn2dx9dJ6NreDbH2LlWo8q5RgPb7SJXGqlWcgvN5GMbOVaieNgmgwsplnNPvcKyC8nno296N8Ib+RajaNgmgwsplnNPvcKyC8nno296N5MquDbACpkX06uJxEMqnSJXGqlWcgvN5EPK2ZZj2NgmgwsplnNPvcKyC8nno297RsPaqVWiO4hGAytb1oI/vcKyC8nno297NIF42CaDCymWc0+60lcbyIeTqrlV06tJVaJ7ideXHjnnw/tdwrOqq8ZjC4nCtpv5FlILyNJSj7knw9vZxsGr2DK2mC0mo995VoIKCRe32qkWQjtZV6fbGVZT+2kXswtZ98N/utJXGvkXs6uJ59IPvKUnG6n2Q+rJ5gJ6DSVH/7gGUyrZZmIbSDK2mC0mg9vYJmOr3SVH/7nWY3rJxsIPvKUnGqlWcgvN5APriXbAerkWo4sJ5ucfXSeja3g2x9mpxmJr2ibDC2l2c6rZlmPfvcKyC8nno296JsMLaCbTq3lyt/+4NsPaqVJxy7mmwwraR7MrqbYD2+0iVxqpVnILzeWiarlmgwvKR7MrqbYD2+0iVxqpVnILzeWiO4gno2ioBoJ7CRZR64gCt/+4NsPaqVJx62hGA8t6R7MrqbYD2+0iVxqpVnILzeTTa3g2wAqZF9OricRDKp0iVxqpVnILzeShKdpHsyuptgPb7SVH/7lXEjsIJsB7CdbACtkWQj+8pnJrWcJXGwg0U8upFlceOWaD+qlXR/otJrJreUZTaQlHpx46srcYTcKyW4gmAyt4R6ceOrKzC2nWQmt5l9KvutJXGpnGgnv597PqrSMwj7mWYg+60lcbSfbSa1lXpx46srILyeejb3uWQyvpVdIbiTYjq3lyt/+4NsPaqVJxC1n3w3i5VqPL6eYCewn2dx9dJ6NreDbH2LlWo8q5RgPb7SJXGqlWcgvN5GMbOVaieNgmgwsplnNPvcKyC8nno296N8Ib+RajaNgmgwsplnNPvcKyC8nno296N5MquDbACpkX06uJxEMqnSJXGqlWcgvN5EPK2ZZj2NgmgwsplnNPvcKyC8nno297RsPaqVWiO4hGAytb1oI/vcKyC8nno297NIF42CaDCymWc0+60lcbyIeTqrlV06tJVaJ7ideXHjnnw/tdwrOqq8ZjC4nCtpv5FlILyNVC7MCM43V0VDcMulI+R0sc132g6jgpfgb+4XlebWacexJLJXCU7pkiZg5MmnlryC94JkbYNapsup3QzAa7JNgfmPtwx6W/4PB3aGxIZG70VoV1ggB2iyrevVXec3b6+Ru8x9G62OB/jJaulRK6xsLnVoVVPLlCDzYnTIZliAfvPUz9/pJArZgPGhjYBLQdAMNI5F53EPbuFuoKChw89SWaZs+v1MNoth2os9DMXr0AxVFpbUcCdvJGXwT/qQIeNmCR+JgR5CYShaou1m+wXCngzNYHaW8MVlBwdHCWxmR1K0inuJKHYhndY/clrTn0H6cZ0JmN4Xoq7r6IptA5bZ8AlT";
    private static String cloudRecognitionServiceServerAddress = "https://9141dfd18788438928f869b3be7a138d.na1.crs.easyar.com:8443";
    private static String apiKey = "9f9cbb43f50dbc50e4d061f0bbb2fc3b";
    private static String apiSecret = "c502c155574610962525ba5380bf678f2eda0ecdca4d0e228f4c0f03ce9e3e07";
    private static String cloudRecognitionServiceAppId = "6db7827f9a21b098a907f87ed09350cf";
    private GLView glView;

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
        if (cloudRecognitionServiceServerAddress == "===PLEASE ENTER YOUR EASYAR CLOUD RECOGNITION SERVICE SERVER ADDRESS HERE===") {
            Toast.makeText(ARActivity.this, "Please enter your cloud server address", Toast.LENGTH_LONG).show();
            return;
        }

        if (!CameraDevice.isAvailable()) {
            Toast.makeText(ARActivity.this, "CameraDevice not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ImageTracker.isAvailable()) {
            Toast.makeText(ARActivity.this, "ImageTracker not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!CloudRecognizer.isAvailable()) {
            Toast.makeText(ARActivity.this, "CloudRecognizer not available.", Toast.LENGTH_LONG).show();
            return;
        }

        glView = new GLView(this, cloudRecognitionServiceServerAddress, apiKey, apiSecret, cloudRecognitionServiceAppId);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
