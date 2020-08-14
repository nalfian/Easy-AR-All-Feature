//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarcustomcamera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import java.io.IOException;
import cn.easyar.CameraParameters;
import cn.easyar.CameraDeviceType;
import cn.easyar.PixelFormat;
import cn.easyar.Vec2I;

public class SampleCamera
{
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture = new SurfaceTexture(0);
    private Camera.Size mPreviewSize;
    private int mPixelFormat = PixelFormat.Unknown;
    private CameraParameters mCameraParameters;

    public boolean open()
    {
        int id = 0;
        mCamera = Camera.open(id);
        if(mCamera != null)
        {
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            int format = params.getPreviewFormat();
            switch (format)
            {
                case ImageFormat.NV21:
                    mPixelFormat = PixelFormat.YUV_NV21;
                    break;
                default:
                    mPixelFormat = PixelFormat.Unknown;
                    break;
            }

            mPreviewSize = params.getPreviewSize();
            mCamera.setParameters(params);

            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(id, info);
            mCameraParameters = CameraParameters.createWithDefaultIntrinsics(new Vec2I(mPreviewSize.width, mPreviewSize.height),CameraDeviceType.Back, info.orientation);
            return true;
        }
        return false;
    }

    private boolean ready()
    {
        return mCamera != null;
    }

    public boolean start(Camera.PreviewCallback callback)
    {
        if (!ready())
            return false;
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.setPreviewCallbackWithBuffer(callback);
            for (int i = 0; i < 16; i++)
                mCamera.addCallbackBuffer(new byte[mPreviewSize.width*mPreviewSize.height*3/2]);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void stop()
    {
        if (!ready())
            return;
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public CameraParameters getmCameraParameters()
    {
        return mCameraParameters;
    }

    public int getPixelFormat()
    {
        return mPixelFormat;
    }
}
