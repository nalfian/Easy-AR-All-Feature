//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarobjecttracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;

import java.util.HashMap;
import android.app.Activity;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.ObjectTracker;

import android.widget.Toast;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARObjectTracking
    *      Package Name: cn.easyar.samples.helloarobjecttracking
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "5cL+v+HR5qP5twYpUUkuIu7oEXE9+R0cupXWxNXwyJTh4M6J1e3ZxJqhzIrG6syI4OLcj47gwsjJ54/Kgu7MldTm363F+uSCgrmcyoLvxIXF7d6D06GXvduhz5PO58GD6efexJrY8MqC9cyUyeLDktOhl72C4MKLzfbDj9T6j7uMod2KwffLidLu3sSa2I+Rye3Jidfwj8qC7syFgt6BxM3syZPM5t7EmtiPlcXt3oOOysCHx+b5lMHgxo/O5I/KgvDIiNPmg6XM7NiC8ubOicftxJLJ7MPEjKHeg87wyMjy5s6J0ufEiMehgcTT5sOVxa3ihMrmzpL08cyFy+rDgYKvj5XF7d6DjtDYlMbizoP08cyFy+rDgYKvj5XF7d6DjtDdh9LwyLXQ4tmPwe/gh9ChgcTT5sOVxa3gidTqwoj08cyFy+rDgYKvj5XF7d6DjsfIiNPm/pbB98SHzM7MloKvj5XF7d6DjsDsovTxzIXL6sOBgt6BxMX73Y/S5vmPzeb+ksHu3cSa7diKzK+Pj9PPwoXB74/cxuLBlcX+gZ2C4diIxO/Ir8Twj9z7oc6IjubMldni38jT4sCWzObeyMjmwYrP4t+JwunIhdT334fD6MSIx6HwyoL1zJTJ4sOS06GXvYLgwovN9sOP1PqPu4yh3YrB98uJ0u7exJrYj4fO59+JyeePu4yhwInE9sGD06GXvYLwyIjT5oOvzeLKg/TxzIXL6sOBgq+PlcXt3oOOwMGJ1ef/g8PsyojJ98SJzqGBxNPmw5XFrf+Dw+zfgsntysSMod6DzvDIyO/hx4PD9/mUweDGj87kj8qC8MiI0+aDtdXxy4fD5vmUweDGj87kj8qC8MiI0+aDtdDi35XF0N2H1OrMiu3i3cSMod6DzvDIyO3s2Y/P7fmUweDGj87kj8qC8MiI0+aDosXt3oPz88ySyeLBq8Hzj8qC8MiI0+aDpeHH+ZTB4MaPzuSPu4yhyJ7Q6t+D9OrAg/P3zIvQoZeI1e/ByoLq3qrP4MyKgrnLh8zwyJuM+I+E1e3JisXKyZWCufbEgt6BxNbi34/B7dmVgrn2xMPswIvV7cSS2aHwyoLzwYfU5cKUzfCP3PuhxInTofDKgu7CgtXvyJWCufbE0+bDlcWt5IvB5Miy0uLOjcntysSMod6DzvDIyOPvwpPE0ciFz+TDj9TqwoiCr4+Vxe3eg47RyIXP8cmPzuSPyoLwyIjT5oOpwunIhdTX34fD6MSIx6GBxNPmw5XFrf6T0uXMhcXX34fD6MSIx6GBxNPmw5XFrf6WwfHeg/PzzJLJ4sGrwfOPyoLwyIjT5oOrz/fEic7X34fD6MSIx6GBxNPmw5XFremDzvDItdDi2Y/B7+CH0KGBxNPmw5XFre6n5Nffh8PoxIjHofDKgubVlsnxyLLJ7si11OLAloK5w5PM74HEyfDhicPiwcSa5cyK0+bQu90l2y3UjSnuWPW+socJtq+EU2MoWIApBdv61v1BwrFmEFh1VcfJoe2adMXNv+LFXf5xOGsbC/HP0AThZ0Q2maVFBsrdxvJkVHUgKfo0vj9K2RIq8WG2swzHUoubj3NnMiHTMVCCkQhYfdLV+3enwmNAKbUyitfEk9AV1TSAHTgK8cfNQgyoNXbr4NQsTg2gNYcoeiW9bJkipwlTm6Zvxr4x/RPeO8K+hGIGyZjK6M8q09ifN7jtHn1kt/cG/MaHdyJD0Kmja99rACSzKugvH7zN2OhEZAicfYnvfdWEIgAvrzv1w9znLdtMGkmyzgrEltrdArKXfn4W8aT/Zd+gg63m";
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

        if (!CameraDevice.isAvailable()) {
            Toast.makeText(ARActivity.this, "CameraDevice not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ObjectTracker.isAvailable()) {
            Toast.makeText(ARActivity.this, "ObjectTracker not available.", Toast.LENGTH_LONG).show();
            return;
        }

        glView = new GLView(this);

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
