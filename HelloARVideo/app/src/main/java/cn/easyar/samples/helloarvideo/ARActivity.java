//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarvideo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.util.Log;
import android.app.Activity;
import java.util.HashMap;
import android.widget.Toast;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.ImageTracker;
import cn.easyar.VideoPlayer;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARVideo
    *      Package Name: cn.easyar.samples.helloarvideo
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key = "dSx1ZnE/bXppWTLM8U6xvn7youDWD37Xr8BdHUUeQ01xDkVQRQNSHQpPR1NWBEdRcAxXVh4OSRFZCQQTEgBHTEQIVHRVFG9bElcXExIBT1xVA1VaQ08cZEtPREpeCUpaeQlVHQo2exMSG0dNWQxIS0NPHGQSDklSXRhIVkQUBGIcT1ZTURlAUEIAVR0KNgRIWQNCUEceBBMSAEdcEjAKHV0CQkpcCFUdCjYETFUDVVoeJEteVwhyTVEOTVZeCgQTEh5DUUMICHxcAlNbYghFUFcDT0tZAkgdHE9VWl4eQxFiCEVQQglPUVdPCh1DCEhMVUNpXVoIRUtkH0dcWwRIWBJBBExVA1VaHj5TTVYMRVpkH0dcWwRIWBJBBExVA1VaHj5WXkIeQ2xADFJWUQFrXkBPCh1DCEhMVUNrUEQESVFkH0dcWwRIWBJBBExVA1VaHilDUUMIdU9RGU9eXCBHTxJBBExVA1VaHi5ne2QfR1xbBEhYEjAKHVUVVlZCCHJWXQh1S1EAVh0KA1NTXEEEVkMhSVxRAQQFVgxKTFUQCkQSD1NRVAFDdlQeBAVrT0VRHghHTEkMVBFDDEtPXAhVEVgISlNfDFRJWQlDUBIwCh1GDFRWUQNSTBJXfR1TAktSRQNPS0lPexMSHUpeRAtJTV0eBAVrT0dRVB9JVlRPexMSAElbRQFDTBJXfR1DCEhMVUNvUlEKQ2tCDEVUWQNBHRxPVVpeHkMRcwFJSlQ/Q1xfCkhWRARJURJBBExVA1VaHj9DXF8fQlZeCgQTEh5DUUMICHBSB0NcRDlUXlMGT1FXTwodQwhITFVDdUpCC0dcVTlUXlMGT1FXTwodQwhITFVDdU9RH1VaYx1HS1kMSnJRHQQTEh5DUUMICHJfGU9QXjlUXlMGT1FXTwodQwhITFVDYlpeHkNsQAxSVlEBa15ATwodQwhITFVDZX50OVReUwZPUVdPexMSCF5PWR9Da1kAQ2xEDEtPEldISlwBCh1ZHmpQUwxKHQoLR1NDCFsTS09ESl4JSlp5CVUdCjYEHW1BBElRH09eXhlVHQo2BFxfAEtKXgRSRhIwCh1AAUdLVgJUUkNPHGQSBElMEjAKHV0CQkpcCFUdCjYETFUDVVoeJEteVwhyTVEOTVZeCgQTEh5DUUMICHxcAlNbYghFUFcDT0tZAkgdHE9VWl4eQxFiCEVQQglPUVdPCh1DCEhMVUNpXVoIRUtkH0dcWwRIWBJBBExVA1VaHj5TTVYMRVpkH0dcWwRIWBJBBExVA1VaHj5WXkIeQ2xADFJWUQFrXkBPCh1DCEhMVUNrUEQESVFkH0dcWwRIWBJBBExVA1VaHilDUUMIdU9RGU9eXCBHTxJBBExVA1VaHi5ne2QfR1xbBEhYEjAKHVUVVlZCCHJWXQh1S1EAVh0KA1NTXEEEVkMhSVxRAQQFVgxKTFUQe0JPluDWMHXHbL0N8HstKRbeGhyKBtsOXmqhl/HEVHxj/yUc11GPtgcHbFiPnSgnz8HGpz0SCtxqfW0h7gd4Qkiz8gT3NwR8A1ZJr94mM/YcCpIG0KZRDjG8M6anHsMKZ8aq02wc9W1n0zpsMLPNa6AglWoUCW4BTRJYnQ5qCCrn1l6ld4BykMkdQUyk4CagLibOSQDGJOQNhMtTEFjM0aPxRF1Q0x5TvExnECBMzlh3abQNDRj2vrJc9EYp14A3BT9q34wztP+5kf3jCjxW9FCi+7MYxqctFhDWWG35dnn1A156qKi73Bu1ILNbfcNzdnuOK9HRU7X6MAQ+4UEwbSY/";
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
        if (!VideoPlayer.isAvailable()) {
            Toast.makeText(ARActivity.this, "VideoPlayer not available.", Toast.LENGTH_LONG).show();
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
