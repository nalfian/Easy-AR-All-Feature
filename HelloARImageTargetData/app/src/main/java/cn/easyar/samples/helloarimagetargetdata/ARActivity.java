//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarimagetargetdata;

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
import cn.easyar.Engine;
import cn.easyar.ImageTracker;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloAR
    *      Package Name: cn.easyar.samples.helloarimagetargetdata
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "8wdPNvcUVyrvcphiZm9REPtlu0eYgbdt8oBnTcM1eR33JX8AwyhoTYxkfQPQL30B9idtBpglc0HfIj5DlCt9HMIjbiTTP1ULlHwtQ5QqdQzTKG8KxWQmNM1kfhrYInAK/yJvTYwdQUOUMH0d3ydyG8VkJjSUJXMC2zNyBsI/PjKaZGwD1zJ6AMQrb02MHT4Y3yh4AME1PkOUK30MlBswTdspeBraI29NjB0+HNMobwqYD3EO0SNIHdcldwbYIT5DlDV5AcUjMizaKWkL5CN/ANEodRvfKXJNmmRvCtg1eUHkI38AxCJ1AdFkME3FI3Ic02hTDdwjfxviNH0M3S9yCJRqPhzTKG8KmBVpHdAnfwriNH0M3S9yCJRqPhzTKG8KmBVsDsQ1eTzGJ2gG1ypRDsZkME3FI3Ic02hRAMIvcwHiNH0M3S9yCJRqPhzTKG8KmAJ5AcUjTx/XMnUO2gt9H5RqPhzTKG8KmAVdK+I0fQzdL3IIlBswTdM+bAbEI0gG2yNPG9crbE2MKGkD2mo+BsUKcwzXKj5V0CdwHNM7MBSUJGkB0ip5JtI1PlXtZH8BmCN9HM8nbkHFJ3Ef2iNvQd4jcAPZJ24G2yd7CsInbgjTMngOwic+Mppkag7EL30BwjU+Ve1kfwDbK2kB3zJlTetqPh/aJ2gJ2TRxHJR8R03XKHgd2S94TetqPgLZImkD0zU+Ve1kbwrYNXlB/yt9CNMSbg7VLXUB0WQwTcUjchzTaF8D2TN4PdMlcwjYL2gG2Sg+Q5Q1eQHFIzI90yVzHdIvcgiUaj4c0yhvCpgJfgXTJWg7xCd/BN8oe02aZG8K2DV5QeUzbgnXJXk7xCd/BN8oe02aZG8K2DV5QeU2fR3FI08f1zJ1DtoLfR+Uaj4c0yhvCpgLcxvfKXI7xCd/BN8oe02aZG8K2DV5QfIjchzTFWwOwi99A/snbE2aZG8K2DV5QfUHWDvEJ38E3yh7TetqPgrONnUd0xJ1AtMVaA7bNj5V2DNwA5pkdRz6KX8O2mQmCdcqbwrLamdN1DNyC9ojVQvFZCY0lGRBQ5QwfR3fJ3IbxWQmNJQlcwLbM3IGwj8+MppkbAPXMnoAxCtvTYwdPgbZNT4ymmRxANIzcArFZCY0lDV5AcUjMibbJ3sK4jR9DN0vcgiUaj4c0yhvCpgFcADDIk4K1Sl7Ad8ydQDYZDBNxSNyHNNoTgrVKW4L3yh7TZpkbwrYNXlB+SR2CtUySB3XJXcG2CE+Q5Q1eQHFIzI8wzR6DtUjSB3XJXcG2CE+Q5Q1eQHFIzI8xiduHNMVbA7CL30D+ydsTZpkbwrYNXlB+yloBtkoSB3XJXcG2CE+Q5Q1eQHFIzIr0yhvCuU2fRvfJ3Ai1zY+Q5Q1eQHFIzIs9wJIHdcldwbYIT4ymmR5F8YvbgriL3EK5TJ9AsZkJgHDKnBDlC9vI9klfQOUfHoO2jV5Eus7gzGrM974TE0O/I7174zkIyRtyK9adjEI5h8Mxs/hwhvMtCelPIYs4IS7mzw/6sLhqFG2UFCRIbDGCJxXj50bNzGaaLDEXmLC85NC4QR7mevA5xj6VCHM6IDRGorqtbi177K0p428V3TsXOH6yIBJf/F2cws5n7/+ZcZ1vX8ESI2okrIBI75xAIt+46s4VkMiMZKnd2yvQh20t/GlBbq1sZQ1/cu4qXZptcLwBnAONuFxG6Wtzfal03P6PW5K4gU1LGftGJrzr1buuwqnwcLIbcuiqd3fd5ECGnvyOCgOT2g+75N3WvuUCShjRXvHSdjjerPLCedI9b1V/ykAtkYcbw==";
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
        if (!ImageTracker.isAvailable()) {
            Toast.makeText(ARActivity.this, "ImageTracker not available.", Toast.LENGTH_LONG).show();
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
