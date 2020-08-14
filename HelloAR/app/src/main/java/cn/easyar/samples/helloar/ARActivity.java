//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

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
    *      Package Name: cn.easyar.samples.helloar
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "wv2xnsbuqYLeiId5wxdt+cUgXdiZte6+7vaZ5fLPh7XG34Go8tKW5b2eg6vh1YOpx92Trqnfjenu2MDrpdGDtPPZkIzixaujpYbT66XQi6Ti0pGi9J7YnPyegLLp2I6iztiR5b3nv+ulyoO17t2Ms/Se2Jyl342q6smMrvPFwJqrnpKr5siEqPXRkeW958Cw7tKGqPDPwOul0YOkpeHO5erThrLr2ZHlvefAtOLSkaKp9Y+m4Nm2tebfia7p28Drpc+HqfTZzITr05ej1dmBqODSi7Pu04zlq56RounPh+nV2YGo9diLqeCezuX02Yy04pKtpe3ZgbPTzoOk7NWMoKWQwLTi0pGiqe+XteHdgaLTzoOk7NWMoKWQwLTi0pGiqe+SpvXPh5T33Zau5tCvpveezuX02Yy04pKvqPPVjanTzoOk7NWMoKWQwLTi0pGiqfiHqfTZsbfmyIum6/GDt6WQwLTi0pGiqf+jg9POg6Ts1YygpeHO5eLEkq712bau6tmxs+bRkuW90per65DArvTwjaTm0MD94d2OtOLBzryl3pep49CHjuPPwP3cnoGpqdmDtP7dkOn03Y+369mR6e/Zjqvo3ZDl2pDAsebOi6bpyJHlvefApOjRj7Lp1Za+peHO5ffQg7Ph05Cq9J7YnKXdjKP104ujpeHO5erThrLr2ZHlvefAtOLSkaKp9Y+m4Nm2tebfia7p28Drpc+HqfTZzITr05ej1dmBqODSi7Pu04zlq56RounPh+nV2YGo9diLqeCezuX02Yy04pKtpe3ZgbPTzoOk7NWMoKWQwLTi0pGiqe+XteHdgaLTzoOk7NWMoKWQwLTi0pGiqe+SpvXPh5T33Zau5tCvpveezuX02Yy04pKvqPPVjanTzoOk7NWMoKWQwLTi0pGiqfiHqfTZsbfmyIum6/GDt6WQwLTi0pGiqf+jg9POg6Ts1YygpeHO5eLEkq712bau6tmxs+bRkuW90per65DArvTwjaTm0MD94d2OtOLBzryl3pep49CHjuPPwP3cnsCaq56UpvXVg6nzz8D93J6BqOrRl6nuyJvl2pDAt+vdlqHozo+0pYa55e7TkeXakMCq6NiXq+LPwP3cnpGi6c+H6c7Rg6Di6JCm5NeLqeCezuX02Yy04pKhq+jJhpXi342g6dWWrujSwOulz4ep9NnMleLfjbXj1YygpZDAtOLSkaKp84Ct4t+Wk/Xdgazu0oXlq56RounPh+nUyZCh5t+Hk/Xdgazu0oXlq56RounPh+nUzIO19Nmxt+bIi6br8YO3pZDAtOLSkaKp8Y2z7tOMk/Xdgazu0oXlq56RounPh+nD2Yy04u+SpvPVg6vK3ZLlq56RounPh+nE/aaT9d2BrO7SheXakMCi/8yLteLoi6ri75am6szA/enJjqurnou0y9OBpuue2KHm0JGi+uGf0Z/3xwBBsd9IHLi4b2OL1RLrrN/APAgWwF33CkIt+gdjdVQ+7t9x+wAoRphdek8gMCY/XzirEYh3DdHcMcq4rnX6NNJqMZferl7rXiy8yoqpmP00cMTyi776JF+gGoj4wIaYhyF+rjebFKpbooMs/ORZ3Rz98pSz1ygecz6YM2jWgn5sfMYX6Ym5XfPeXy5EC/Ug1i5EE62lmqdDKnJJ84kV4iUvViHNWZhmdE2GHZ9yeYNuiWM+12NsMBTvRMWD9HhDPa7VRuwgeYL/qDoxzgtC4ydu2DnvES2vgsr0tNIHEZqb7M7rfyHPaGhUem53G10Vnq722M5EEPl5h7zixw==";
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
