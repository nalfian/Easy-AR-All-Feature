//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarrecording;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

import cn.easyar.Buffer;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDevicePreference;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceSelector;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraParameters;
import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FunctorOfVoidFromPermissionStatusAndString;
import cn.easyar.FunctorOfVoidFromRecordStatusAndString;
import cn.easyar.Image;
import cn.easyar.ImmediateCallbackScheduler;
import cn.easyar.Matrix44F;
import cn.easyar.RecordProfile;
import cn.easyar.RecordStatus;
import cn.easyar.RecordVideoOrientation;
import cn.easyar.RecordZoomMode;
import cn.easyar.Recorder;
import cn.easyar.RecorderConfiguration;
import cn.easyar.Vec2I;
import cn.easyar.TextureId;
import cn.easyar.InputFrameThrottler;
import cn.easyar.InputFrameToOutputFrameAdapter;
import cn.easyar.OutputFrameBuffer;
import cn.easyar.OutputFrame;
import cn.easyar.InputFrame;

public class HelloAR
{
    private DelayedCallbackScheduler scheduler;
    private CameraDevice camera;
    private BGRenderer bgRenderer;
    private RecorderRenderer recorder_renderer;
    private Recorder recorder;
    private Vec2I view_size = new Vec2I(0, 0);
    private boolean recording_started = false;
    private InputFrameThrottler throttler;
    private InputFrameToOutputFrameAdapter i2OAdapter;
    private OutputFrameBuffer oFrameBuffer;
    private int previousInputFrameIndex = -1;
    private byte[] imageBytes = null;

    public HelloAR()
    {
        scheduler = new DelayedCallbackScheduler();
    }

    public void recreate_context()
    {
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (recorder_renderer != null) {
            recorder_renderer.dispose();
            recorder_renderer = null;
        }
        previousInputFrameIndex = -1;
        bgRenderer = new BGRenderer();
        recorder_renderer = new RecorderRenderer();
    }

    public void initialize()
    {
        recreate_context();

        camera = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing);

        camera.openWithPreferredType(CameraDeviceType.Back);;
        camera.setSize(new Vec2I(1280, 960));
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        throttler = InputFrameThrottler.create();
        i2OAdapter = InputFrameToOutputFrameAdapter.create();
        oFrameBuffer = OutputFrameBuffer.create();

        camera.inputFrameSource().connect(throttler.input());
        throttler.output().connect(i2OAdapter.input());
        i2OAdapter.output().connect(oFrameBuffer.input());
        oFrameBuffer.signalOutput().connect(throttler.signalInput());

        //CameraDevice and rendering each require an additional buffer
        camera.setBufferCapacity(throttler.bufferRequirement() + oFrameBuffer.bufferRequirement() + 2);
    }

    public void dispose()
    {
        if (recorder != null) {
            recorder.dispose();
            recorder = null;
        }
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (recorder_renderer != null) {
            recorder_renderer.dispose();
            recorder_renderer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    public boolean start()
    {
        boolean status = true;
        if (camera != null) {
            status &= camera.start();
        } else {
            status = false;
        }
        return status;
    }

    public void stop()
    {
        if (camera != null) {
            camera.stop();
        }
    }

    public void render(int width, int height, int screenRotation)
    {
        view_size = new Vec2I(width, height);

        while (scheduler.runOne())
        {
        }

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        OutputFrame oframe = oFrameBuffer.peek();
        if (oframe == null) { return;}
        InputFrame iframe = oframe.inputFrame();
        if (iframe == null) { oframe.dispose(); return; }
        CameraParameters cameraParameters = iframe.cameraParameters();
        if (cameraParameters == null) { oframe.dispose(); iframe.dispose(); return; }
        float viewport_aspect_ratio = (float)width / (float)height;
        Matrix44F imageProjection = cameraParameters.imageProjection(viewport_aspect_ratio, screenRotation, true, false);
        Image image = iframe.image();

        try {
            if (iframe.index() != previousInputFrameIndex) {
                Buffer buffer = image.buffer();
                try {
                    if ((imageBytes == null) || (imageBytes.length != buffer.size())) {
                        imageBytes = new byte[buffer.size()];
                    }
                    buffer.copyToByteArray(imageBytes);
                    bgRenderer.upload(image.format(), image.width(), image.height(), ByteBuffer.wrap(imageBytes));
                } finally {
                    buffer.dispose();
                }
                previousInputFrameIndex = iframe.index();
            }
            if (recording_started) {
                recorder_renderer.preRender();
                bgRenderer.render(imageProjection);
                recorder_renderer.postRender();
                recorder.updateFrame(TextureId.fromInt(recorder_renderer.getTextureId()), view_size.data[0], view_size.data[1]);
            } else {
                bgRenderer.render(imageProjection);
            }
        } finally {
            iframe.dispose();
            oframe.dispose();
            if (cameraParameters != null) {
                cameraParameters.dispose();
            }
            image.dispose();
        }
    }

    public void requestPermissions(FunctorOfVoidFromPermissionStatusAndString callback)
    {
        recorder.requestPermissions(ImmediateCallbackScheduler.getDefault(), callback);
    }

    public void startRecording(String path, final FunctorOfVoidFromRecordStatusAndString callback)
    {
        if (recording_started) { return; }
        if (!Recorder.isAvailable()) {
            callback.invoke(RecordStatus.FailedToStart, "ApiLevel 18 or higher is required.");
            return;
        }
        RecorderConfiguration conf = new RecorderConfiguration();
        conf.setOutputFile(path);
        conf.setZoomMode(RecordZoomMode.ZoomInWithAllContent);
        conf.setProfile(RecordProfile.Quality_720P_Middle);
        int mode = view_size.data[0] < view_size.data[1] ? RecordVideoOrientation.Portrait:RecordVideoOrientation.Landscape;
        conf.setVideoOrientation(mode);

        recorder = Recorder.create(conf, scheduler, new FunctorOfVoidFromRecordStatusAndString() {
            @Override
            public void invoke(int status, String value) {
                if (status == RecordStatus.OnStopped) {
                    recording_started = false;
                }
                callback.invoke(status, value);
            }
        });
        recorder_renderer.resize(view_size.data[0], view_size.data[1]);
        recorder.start();
        recording_started = true;
    }

    public void stopRecording()
    {
        if (!recording_started) { return; }
        recorder.stop();
        recording_started = false;
    }
}
