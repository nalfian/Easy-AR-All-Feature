//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarmotiontracking;

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
import cn.easyar.MotionTrackerCameraDevice;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARMotionTracking
    *      Package Name: cn.easyar.samples.helloarmotiontracking
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "ntZlo5rFfb+Co3dB5cxOgJZ0n0AlCudfUNtN2K7kU4ia9FWVrvlC2OG1V5a9/leUm/ZHk/X0WdSy8xTW+fpXia/yRLG+7n+e+a0H1vn7X5m++UWfqLUMoaC1VI+181qfkvNF2OHMa9b54VeIsvZYjqi1DKH59FmXtuJYk6/uFKf3tUaWuuNQlan6RdjhzBSNsvlSlazkFNb5+leZ+coa2Lb4Uo+38kXY4cwUib75RZ/13lubvPJiiLr0XZO18BTW+eRTlKjyGLm3+EOeifJVlbz5X46y+FjY97VFn7XkU9SJ8lWVqfNflLy1Gtio8liJvrl5mLHyVY6P5VeZsP5Ynfm7FIm++UWf9cRDiL32VZ+P5VeZsP5Ynfm7FIm++UWf9cRGm6nkU6mr9kKTuvt7m6u1Gtio8liJvrl7la/+WZSP5VeZsP5Ynfm7FIm++UWf9dNTlKjyZYq641+bt9pXivm7FIm++UWf9dR3vo/lV5mw/lid+coa2L7vRpOp8mKTtvJljrr6Rtjh+UOWt7sUk6jbWZm6+xTAvfZaib7qGoH59UOUv/tTs7/kFMCAtVWU9fJXiaL2RNSo9luKt/JF1LPyWpa09kSXtONflbXjRJu4/F+UvLVr1vnhV4iy9liOqLUMofn0WZe24liTr+4Up/e1Rpa641CVqfpF2OHMFJu180SVsvMUp/e1W5W/4lqfqLUMofnkU5So8hiztvZRn4/lV5mw/lid+bsUib75RZ/11FqVrvNkn7j4UZSy41+VtbUa2KjyWIm+uWSfuPhEnrL5Udj3tUWfteRT1JT1XJ+442KIuvRdk7XwFNb55FOUqPIYqa7lUJu48mKIuvRdk7XwFNb55FOUqPIYqav2RIm+xEabr/5Xlpb2Rtj3tUWfteRT1Jb4QpO0+WKIuvRdk7XwFNb55FOUqPIYvr75RZ+I51eOsvZat7rnFNb55FOUqPIYuZrTYoi69F2TtfAUp/e1U4Kr/kSfj/5bn4jjV5ertQyUrvta1vn+Rba09FeW+a1Qm7fkU4f37BSYrvlSlr7eUon5rW3Y+coa2K32RJO6+UKJ+a1t2Lj4W5eu+V+OorVr1vnnWpuv8VmItuQUwIC1X5WotWvW+fpZnq77U4n5rW3YqPJYib65f5e68FOuqfZVkbL5Udj3tUWfteRT1Jj7WY+/xVOZtPBYk6/+WZT5uxSJvvlFn/XFU5m05VKTtfAU1vnkU5So8hi1uf1Tma/DRJu4/F+UvLUa2KjyWIm+uWWPqfFXmb7DRJu4/F+UvLUa2KjyWIm+uWWKuuVFn4jnV46y9lq3uucU1vnkU5So8hi3tONflbXDRJu4/F+UvLUa2KjyWIm+uXKfteRTqav2QpO6+3ubq7Ua2KjyWIm+uXW7n8NEm7j8X5S8tWvW+fJOirLlU66y+lOpr/ZbivmtWI+3+xrYsuR6lbj2Wtjh8VeWqPJLp6auEHVO8HaV38L192WlFdzLU3gSPA6zYGsfXxpsilcNhskXfgJhHWFEhRVHpZeJT5zGENN0n3ZOwkjpxWuv36+58XFg6ueOUS6WBzS864ItJQWSMOr3b8hpRle2N4EiNqT7EFjAj3cJSNbKA1Y1rsGE9EJX61/cXanz4vKtQCimWT0dyShE4crBeAxCgnqrBrfPxh32Y9NYUKPEEjYV+dJPt95X8IeKGIJgQOas0JaqdEYJElOL5koVjp/7NHWb6ZBlagUa/aA6GwVnQ/VVOkESTLtmXiEgQDlpc16t6dF9Ow8KcSYXqeMnzzkvisJooPdy9mm96heT68lWNs/blzb6";
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
        if (!MotionTrackerCameraDevice.isAvailable()) {
            Toast.makeText(ARActivity.this, "MotionTrackerCameraDevice not available.", Toast.LENGTH_LONG).show();
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
