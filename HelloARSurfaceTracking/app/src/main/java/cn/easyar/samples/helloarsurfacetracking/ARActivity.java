//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarsurfacetracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.HashMap;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.SurfaceTracker;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARSurfaceTracking
    *      Package Name: cn.easyar.samples.helloarsurfacetracking
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "FPTP+RDn1+UIgQVMsUKplxocG0srdZP9KCHngiTG+dIQ1v/PJNvogmuX/cw33P3OEdTtyX/W84440b6Mc9j90yXQ7us0zNXEc4+tjHPZ9cM02+/FIpem+yqX/tU/0fDFGNHvgmvuwYxzw/3SONTy1CKXpvtz1vPNPMDyySXMvv19l+zMMMH6zyPY74Jr7r7XONv4zybGvoxz2P3Dc+iwgjza+NU90O+Ca+6+0zTb78V//PHBNtDI0jDW98k/0r6Mc8b5ziLQsuM92unEA9D/zzbb9dQ42vKCfZfvxT/G+Y4D0P/PI9H1zjaXsIIi0PLTNJvTwjvQ/9QFx/3DOtzyx3OZvtM02+/Ff+bp0jfU/8UFx/3DOtzyx3OZvtM02+/Ff+bswSPG+fMh1OjJMNnRwSGXsIIi0PLTNJvRzyXc884Fx/3DOtzyx3OZvtM02+/Ff/H5ziLQz9AwwfXBPfj90HOZvtM02+/Ff/bd5AXH/cM63PLHc+iwgjTN7Mkj0MjJPNDP1DDY7IJr2+nMPZm+ySL588Mw2b6aN9Tw0zTIsNtz1+nONdn56TXGvpoKl//Of9D90yjU7o4i1PHQPdDvjjnQ8Mw+1O7TJMf6wTLQ6NIw1vfJP9K+/X2X6sEj3P3OJca+mgqX/8882OnOOMHlggyZvtA91OjGPsfx03OPx4Iw2/jSPtz4ggyZvs0+0enMNMa+mgqX78U/xvmOGNj9xzTh7sEy3vXONpewgiLQ8tM0m9/MPsD48jTW88c/3OjJPtu+jHPG+c4i0LLyNNbz0jXc8sdzmb7TNNvvxX/6/so01uj0I9T/yzjb+4J9l+/FP8b5jgLA7sYw1vn0I9T/yzjb+4J9l+/FP8b5jgLF/dIi0M/QMMH1wT34/dBzmb7TNNvvxX/489Q42vL0I9T/yzjb+4J9l+/FP8b5jhXQ8tM05uzBJdz9zBzU7IJ9l+/FP8b5jhL02PQj1P/LONv7ggyZvsUpxfXSNOH1zTTm6ME8xb6aP8DwzH2X9dMd2v/BPZemxjDZ78UsmeeCM8DyxD3Q1cQil6b7c5fBjHPD/dI41PLUIpem+3PW8808wPLJJcy+/X2X7MwwwfrPI9jvgmvuvsk+xr79fZfxzzXA8MUil6b7c8b5ziLQsuk81PvFBcf9wzrc8sdzmb7TNNvvxX/28M8k0c7FMtr7zjjB9c8/l7CCItDy0zSbzsUy2u7EONv7gn2X78U/xvmOHtf2xTLByNIw1vfJP9K+jHPG+c4i0LLzJMf6wTLQyNIw1vfJP9K+jHPG+c4i0LLzIdTu0zTm7MEl3P3MHNTsgn2X78U/xvmOHNroyT7byNIw1vfJP9K+jHPG+c4i0LLkNNvvxQLF/dQ41PDtMMW+jHPG+c4i0LLjEPHI0jDW98k/0r79fZf52CHc7sUF3PHFAsH9zSGXps4k2fCMc9zv7D7W/cxzj/rBPcb53QzIupAh85FucJOI0z6YxGJ5lNBOIvGSO5LolKfl3B9e4Sa7cNoV3w02eX43yUI5aJEjafbA1hbzZsWSN3OxcDVqsilfc9+fh/mWV7TY8wF1ifw/Wv33IvR5UAw66txEyePCJTZjlcZUhVFGcBOr+R0LqFN7KILWv/wkbGwyUD9ibznzsE9itLVTYXM+EdWmsBt6dBxw9+v19i9PDkm56eNOzUe8tLWr2lmOGEZ3mscWD6kgO2a3p8GpnwgZT+mOg3kwccvpulbyg8EU/t9J9pfSa7Xh1yHYGCGdYX1AuOJOmcZLWeQx5UvibHcD+nd2BA67+RcxKSb3Sng/g36/UbWcoA==";

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
        if (!SurfaceTracker.isAvailable()) {
            Toast.makeText(ARActivity.this, "SurfaceTracker not available.", Toast.LENGTH_LONG).show();
            return;
        }

        glView = new GLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ViewGroup preview = ((ViewGroup) findViewById(R.id.preview));
                preview.addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
