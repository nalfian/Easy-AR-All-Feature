//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarmultitargetmt;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.app.Activity;
import android.widget.Toast;

import java.util.HashMap;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.ImageTracker;

public class ARActivity extends Activity {

    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARMultiTargetMT
    *      Package Name: cn.easyar.samples.helloarmultitargetmt
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "GK1Zehy+QWYE2IqgS564CBiFlTbivwzBUORxASifb1Ecj2lMKIJ+AWfOa087hWtNHY17SnOPZQ00iCgPf4FrUCmJeGg4lUNHf9Y7D3+AY0A4gnlGLs4weCbOaFYziGZGFIh5AWe3Vw9/mmtRNI1kVy7OMHh/j2VOMJlkSimVKH5xznpPPJhsTC+BeQFntyhUNIJuTCqfKA9/gWtAf7EmATCDblYxiXkBZ7coUDiCeUZzpWdCOoleUTyPYUoziygPf59vTS6JJGAxg39HD4lpTDqCY1c0g2QBcc55RjOfbw0PiWlML4hjTTrOJgEuiWRQOMJFQTeJaVcJnmtANoVkRH/AKFA4gnlGc79/UTuNaUYJnmtANoVkRH/AKFA4gnlGc796Qi+fb3AtjX5KPIBHQi3OJgEuiWRQOMJHTCmFZU0JnmtANoVkRH/AKFA4gnlGc6hvTS6JWVM8mGNCMaFrU3/AKFA4gnlGc69LZwmea0A2hWREf7EmATiUekoviV5KMIlZVzyBegFngn9PMcAoSi6gZUA8gCgZO41mUDiRJlh/jn9NOYBvajmfKBkGzmlNc4lrUCSNeA0ujWdTMYl5DTWJZk8yjXhOKIB+SimNeEQ4mGdXf7EmASuNeEo8gn5Qf9ZRAT6DZ04ogmNXJM5XD3+cZkIpimVRMJ8oGQbOa005nmVKOc5XD3+BZUcogG9Qf9ZRAS6JZFA4wkNOPItvdy+NaUg0gm0Bcc55RjOfbw0egGVWOb5vQDKLZEophWVNf8AoUDiCeUZzvm9AMp5uSjOLKA9/n29NLokkbD+Gb0ApuHhCPodjTTrOJgEuiWRQOMJZVi+Ka0A4uHhCPodjTTrOJgEuiWRQOMJZUzyeeUYOnGtXNI1mbjycKA9/n29NLokkbjKYY0wzuHhCPodjTTrOJgEuiWRQOMJORjOfb3AtjX5KPIBHQi3OJgEuiWRQOMJJYhm4eEI+h2NNOs5XD3+JclM0nm93NIFvcCmNZ1N/1mRWMYAmATSfRkw+jWYBZ4prTy6Jdw8mzmhWM4hmRhSIeQFntygBAMAoVTyeY0IzmHkBZ7coQDKBZ1YzhX5af7EmAS2Aa1c7g3hOLs4weH+FZVB/sSYBMINuVjGJeQFntyhQOIJ5RnOlZ0I6iV5RPI9hSjOLKA9/n29NLokkYDGDf0cPiWlMOoJjVzSDZAFxznlGM59vDQ+JaUwviGNNOs4mAS6JZFA4wkVBN4lpVwmea0A2hWREf8AoUDiCeUZzv39RO41pRgmea0A2hWREf8AoUDiCeUZzv3pCL59vcC2Nfko8gEdCLc4mAS6JZFA4wkdMKYVlTQmea0A2hWREf8AoUDiCeUZzqG9NLolZUzyYY0IxoWtTf8AoUDiCeUZzr0tnCZ5rQDaFZER/sSYBOJR6Si+JXkowiVlXPIF6AWeCf08xwChKLqBlQDyAKBk7jWZQOJFXXkw1LBlIe5q39T7xvuR6pJgwsvE4Lbc1jAdTXk7jVYeQUjnRyk24jr7rDGvCQzbjz6Gtmubs93/qYvW/5It7oQhVraLHBuIFX8I31og/vSqhbGcUKsoNsRJNYEzYLaD9kAHkK3F/lj7MoHejg9sPqyjwxL9Osk1M6BzNhlKMEWA5zMw0Oti2st9Axk4wS8j0Tt58WDz05quYT1XsVDbged65takI2hDn4OuXSQtP3XUlo7rFgy69MLbvvmGVM68xTeyc7LGHsRJWMeBDDb7QCXy97VPiLfvowZMoPBytbS+rK4gdHjxf9yoAmHjhclYB6xRMAKJhRxF25x6p913sCiM=";
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
