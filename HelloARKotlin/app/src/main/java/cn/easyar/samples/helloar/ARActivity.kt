//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.app.Activity
import android.view.ViewGroup
import android.view.WindowManager
import android.util.Log
import android.widget.Toast
import cn.easyar.CameraDevice

import java.util.HashMap

import cn.easyar.Engine
import cn.easyar.ImageTracker

class ARActivity : Activity() {
    companion object {
        //
        // Steps to create the key for this sample:
        //  1. login www.easyar.com
        //  2. create app with
        //      Name: HelloAR
        //      Package Name: cn.easyar.samples.helloar
        //  3. find the created item in the list and show key
        //  4. set key string bellow
        //
        private val key = "===PLEASE ENTER YOUR KEY HERE==="
    }
    private var glView: GLView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.")
            Toast.makeText(this@ARActivity, Engine.errorMessage(), Toast.LENGTH_LONG).show()
            return
        }

        if (!CameraDevice.isAvailable()) {
            Toast.makeText(this@ARActivity, "CameraDevice not available.", Toast.LENGTH_LONG).show()
            return
        }
        if (!ImageTracker.isAvailable()) {
            Toast.makeText(this@ARActivity, "ImageTracker not available.", Toast.LENGTH_LONG).show()
            return
        }

        glView = GLView(this)

        requestCameraPermission(object : PermissionCallback {
            override fun onSuccess() {
                findViewById<ViewGroup>(R.id.preview).addView(glView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            override fun onFailure() {}
        })
    }

    private interface PermissionCallback {
        fun onSuccess()
        fun onFailure()
    }

    private val permissionCallbacks = HashMap<Int, PermissionCallback>()
    private var permissionRequestCodeSerial = 0
    @TargetApi(23)
    private fun requestCameraPermission(callback: PermissionCallback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val requestCode = permissionRequestCodeSerial
                permissionRequestCodeSerial += 1
                permissionCallbacks[requestCode] = callback
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
            } else {
                callback.onSuccess()
            }
        } else {
            callback.onSuccess()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissionCallbacks.containsKey(requestCode)) {
            val callback = permissionCallbacks[requestCode]!!
            permissionCallbacks.remove(requestCode)
            var executed = false
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true
                    callback.onFailure()
                }
            }
            if (!executed) {
                callback.onSuccess()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        if (glView != null) {
            glView!!.onResume()
        }
    }

    override fun onPause() {
        if (glView != null) {
            glView!!.onPause()
        }
        super.onPause()
    }
}
