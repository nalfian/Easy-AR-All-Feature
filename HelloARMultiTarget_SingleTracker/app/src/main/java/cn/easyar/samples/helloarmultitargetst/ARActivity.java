//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarmultitargetst;

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
import android.widget.Toast;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.ImageTracker;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARMultiTargetST
    *      Package Name: cn.easyar.samples.helloarmultitargetst
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "lfMZ95HgAeuJhvZ/+2y6Q5IC1QfPqBgqI8sxjKXBL9yR0SnBpdw+jOqQK8K22yvAkNM7x/7RJYC51miC8t8r3aTXOOW1ywPK8oh7gvLeI8213DnLo5Bw9auQKNu+1ibLmdY5jOrpF4LyxCvcudMk2qOQcPXy0SXDvcckx6TLaPP8kDrCscYswaLfOYzq6WjZudwuwafBaILy3yvN8u9mjL3dLtu81zmM6ulo3bXcOcv++yfPt9ce3LHRIce+1WiC8sEvwKPXZO283T/KgtcpwbfcI9q53SSM/JA5y77BL4CC1ynBotYjwLeQZoyj1yTdtZwFzLrXKdqEwCvNu9skyfKeaN213DnL/uE/3LbTKcuEwCvNu9skyfKeaN213DnL/uE6z6LBL/2g0z7Hsd4Hz6CQZoyj1yTdtZwHwaTbJcCEwCvNu9skyfKeaN213DnL/vYvwKPXGd6xxiPPvP8r3vKeaN213DnL/vEL6oTAK8272yTJ8u9mjLXKOsei1x7HvdcZ2rHfOozq3D/CvJ5ox6P+Jc2x3miUttMm3bXPZtXy0D/AtN4v57TBaJSLkCnA/tcr3anTOICj0yfevNc5gLjXJsK/0zjDpd4+x6TTOMm1xjna8u9mjKbTOMex3D7d8ogRjLPdJ8Ol3CPaqZAXgvLCJs+k1CXcvcFolIuQK8C0wCXHtJAXgvLfJcql3i/d8ogRjKPXJN21nAPDsdUv+qLTKcW53C2M/JA5y77BL4CT3iXbtOAvzb/VJMek2yXA8p5o3bXcOcv+4C/Nv8Aux77VaILywS/Ao9dk4bLYL82k5jjPs9kjwLeQZoyj1yTdtZwZ26LUK8215jjPs9kjwLeQZoyj1yTdtZwZ3rHAOcuDwivaudMm47HCaILywS/Ao9dk47/GI8G+5jjPs9kjwLeQZoyj1yTdtZwOy77BL/2g0z7Hsd4Hz6CQZoyj1yTdtZwJ75TmOM+z2SPAt5AXgvLXMt65wC/6ud8v/aTTJ97yiCTbvN5mjLnBBsGz0yaM6tQrwqPXN4KrkCjbvtYmy5nWOYzq6WiMjZ5o2LHAI8++xjmM6ulozb/fJ9u+2z7X8u9mjKDeK9q23TjDo5Bw9fLbJd3y72aMvd0u27zXOYzq6Wjdtdw5y/77J8+31x7csdEhx77VaILywS/Ao9dk7bzdP8qC1ynBt9wj2rndJIz8kDnLvsEvgILXKcGi1iPAt5BmjKPXJN21nAXMutcp2oTAK8272yTJ8p5o3bXcOcv+4T/cttMpy4TAK8272yTJ8p5o3bXcOcv+4TrPosEv/aDTPsex3gfPoJBmjKPXJN21nAfBpNslwITAK8272yTJ8p5o3bXcOcv+9i/Ao9cZ3rHGI8+8/yve8p5o3bXcOcv+8QvqhMArzbvbJMny72aMtco6x6LXHse91xnasd86jOrcP8K8nmjHo/4lzbHeaJS20ybdtc8X076Eft1V15HbcCiequU+uATQCoXmoUVnVfIA8d0sACsdvw21sJYGbpreRa3DVsTS7kCijYGpqE1C4QCg77QtjeJQ32px75D79y54x4iymqzfJejn/4bA/JCqDxLexCeg1rw0iZkoVfjTH2cnVkPqX5fB7ZpmHeh0Rfgd+9Xfllbbu0+AsxJtRUkRULmipZ5hetax27VlqV7EElKJ4TVNI0ILHNXz5/QPlW8n4ldoRhAq4EflnHeMRwwRc0h+J+A/k+w5yCICFbZeahEYjM4vg2UwHHmarqaouLuGgsEgVQVpkYeNqaF6skYmQAQyJTRhvyt5Ra3uS3cJEldA+NCySq4=";
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
