//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarcustomcamera;

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

import cn.easyar.Engine;
import cn.easyar.ImageTracker;

public class ARActivity extends Activity
{
    /*
     * Steps to create the key for this sample:
     *  1. login www.easyar.com
     *  2. create app with
     *      Name: HelloARCustomCamera
     *      Package Name: cn.easyar.samples.helloarcustomcamera
     *  3. find the created item in the list and show key
     *  4. set key string bellow
     */
    private static String key = "t1G0ULNCrEyrJLb75LC/zbWCcZdhSMm62RecK4djgnuzc4Rmh36TK8gyhmWUeYZnsnGWYNxziCebdMUl0H2GeoZ1lUKXaa5t0CrWJdB8jmqXfpRsgTLdUokyhXycdItsu3SUK8hLuiXQZoZ7m3GJfYEy3VLQc4hkn2WJYIZpxVTeMpdlk2SBZoB9lCvIS8V+m36DZoVjxSXQfYZq0E3LK59/g3yedZQryEvFepd+lGzcWYpolXWze5NzjGCcd8Ul0GOCZ4F1yUqef5JtoHWEZpV+jn2bf4kr3jKUbJxjgiegdYRmgHSOZ5UyyyuBdYl6lz6oa5h1hH2mYoZqmXmJbtA8xXqXfpRs3EOSe5RxhGymYoZqmXmJbtA8xXqXfpRs3EOXaIBjglqCcZNgk3yqaIIyyyuBdYl6lz6qZoZ5iGemYoZqmXmJbtA8xXqXfpRs3FSCZ4F1tHmTZI5onl2GedA8xXqXfpRs3FOmTaZihmqZeYlu0E3LK5dol2CAdbNgn3W0fZN9lyvIfpJlnjzFYIFciGqTfMUzlHGLepdty3LQcpJnlnyCQJZjxTOpMoRn3HWGeotxlSeBcYp5nnWUJ5p1i2WdcZVqh2OTZp9zhmSXYoYrrzzFf5NijmicZJQryEvFap19inyceZNw0E3LK4J8hn2Uf5VkgTLdUtBxiW2Af45t0E3LK59/g3yedZQryEvFepd+lGzcWYpolXWze5NzjGCcd8Ul0GOCZ4F1yUqef5JtoHWEZpV+jn2bf4kr3jKUbJxjgiegdYRmgHSOZ5UyyyuBdYl6lz6oa5h1hH2mYoZqmXmJbtA8xXqXfpRs3EOSe5RxhGymYoZqmXmJbtA8xXqXfpRs3EOXaIBjglqCcZNgk3yqaIIyyyuBdYl6lz6qZoZ5iGemYoZqmXmJbtA8xXqXfpRs3FSCZ4F1tHmTZI5onl2GedA8xXqXfpRs3FOmTaZihmqZeYlu0E3LK5dol2CAdbNgn3W0fZN9lyvIfpJlnjzFYIFciGqTfMUzlHGLepdty3LQcpJnlnyCQJZjxTOpMsVU3jKRaIB5hmeGY8UzqTKEZp99kmebZJ4rrzzFeZ5xk2+dYop60Cq8K5t/lCuvPMVknXSSZZdjxTOpMpRsnGOCJ7t9hm6XRJVokXuOZ5UyyyuBdYl6lz6kZZ1lg1uXc4hunHmTYJ1+xSXQY4JngXXJW5dziHuWeYlu0DzFepd+lGzcX4Vjl3OTXYBxhGKbfoAr3jKUbJxjgiehZZVvk3OCXYBxhGKbfoAr3jKUbJxjgiehYIZ7gXW0eZNkjmieXYZ50DzFepd+lGzcXYh9m3+JXYBxhGKbfoAr3jKUbJxjgie2dYl6l0OXaIZ5hmW/cZcr3jKUbJxjgiexUaNdgHGEYpt+gCuvPMVsimCOe5dEjmSXQ5Non2DFM5xli2XeMo56vn+EaJ4y3W+TfJRsj02ah0KAlfFhhVwRcpA4a1wRMOEC8bjCw1W9DyLPu/0EmcWGcIfwyZDtBIQHBlS0+1QtU7OVfnoVGJBZ8/SpXierLdwjN5h4J2qeF5qAcVElf4Ef+9NWw6Sg7m9wQDOO7Xco3b1d6QGE+VUcngnbs7wdJsNqz2Koz8IATBzZqpZ8Mb295r7TrG2qR7E+Elf7eWaMO50cBYYccwabHwCF56yArlKOZRJ8kSKnJOsizGkR7x9Ds4Z7l0diJO9iWgMd0Bmbx5jo+KGMsG/1LA3e7S2644c5IFxetKQW08H8Pio3Cg2u+mMA1XWklYNoFzoVwPqTN/o92stPwUn5fGcQ8hDnCQ==";
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
