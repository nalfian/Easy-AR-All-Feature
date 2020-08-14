//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarcustomcamera;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.hardware.Camera;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.Log;

import cn.easyar.Buffer;
import cn.easyar.CameraParameters;
import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FunctorOfVoid;
import cn.easyar.Image;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.ImageTrackerMode;
import cn.easyar.ImageTrackerResult;
import cn.easyar.InputFrame;
import cn.easyar.InputFrameSink;
import cn.easyar.InputFrameThrottler;
import cn.easyar.InputFrameToFeedbackFrameAdapter;
import cn.easyar.Matrix44F;
import cn.easyar.FunctorOfVoidFromTargetAndBool;
import cn.easyar.OutputFrame;
import cn.easyar.OutputFrameBuffer;
import cn.easyar.OutputFrameFork;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.FrameFilterResult;
import cn.easyar.Vec2F;

public class HelloAR
{
    private DelayedCallbackScheduler scheduler;
    private SampleCamera cameraDevice;
    private ArrayList<ImageTracker> trackers;
    private BGRenderer bgRenderer;
    private BoxRenderer boxRenderer;
    private InputFrameThrottler throttler;
    private InputFrameToFeedbackFrameAdapter i2FAdapter;
    private OutputFrameBuffer outputFrameBuffer;
    private InputFrameSink sink;
    private OutputFrameFork outputFrameFork;
    private int previousInputFrameIndex = -1;
    private byte[] imageBytes = null;

    public HelloAR()
    {
        scheduler = new DelayedCallbackScheduler();
        trackers = new ArrayList<ImageTracker>();
    }

    private void loadFromImage(ImageTracker tracker, String path, String name)
    {
        ImageTarget target = ImageTarget.createFromImageFile(path,StorageType.Assets, name, "", "", 1.0f);
        if(target == null) {
            Log.e("HelloAR","target create failed or key is not correct");
            return;
        }
        tracker.loadTarget(target, scheduler, new FunctorOfVoidFromTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    public void recreate_context()
    {
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (boxRenderer != null) {
            boxRenderer.dispose();
            boxRenderer = null;
        }
        previousInputFrameIndex = -1;
        bgRenderer = new BGRenderer();
        boxRenderer = new BoxRenderer();
    }

    public void initialize()
    {
        recreate_context();

        cameraDevice = new SampleCamera();
        cameraDevice.open();
        throttler = InputFrameThrottler.create();
        i2FAdapter = InputFrameToFeedbackFrameAdapter.create();
        outputFrameBuffer = OutputFrameBuffer.create();
        outputFrameFork = OutputFrameFork.create(2);
        ImageTracker tracker = ImageTracker.createWithMode(ImageTrackerMode.PreferQuality);
        tracker.setSimultaneousNum(2);
        loadFromImage(tracker, "sightplus/argame00.jpg", "argame00");
        loadFromImage(tracker, "sightplus/argame01.jpg", "argame01");
        loadFromImage(tracker, "sightplus/argame02.jpg", "argame02");
        loadFromImage(tracker, "sightplus/argame03.jpg", "argame03");
        loadFromImage(tracker, "idback.jpg", "idback");
        loadFromImage(tracker, "namecard.jpg", "namecard");
        trackers.add(tracker);
        sink = throttler.input();
        throttler.output().connect(i2FAdapter.input());
        i2FAdapter.output().connect(tracker.feedbackFrameSink());
        tracker.outputFrameSource().connect(outputFrameFork.input());
        outputFrameFork.output(0).connect(outputFrameBuffer.input());
        outputFrameFork.output(1).connect(i2FAdapter.sideInput());
        outputFrameBuffer.signalOutput().connect(throttler.signalInput());
    }

    public void dispose()
    {
        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (boxRenderer != null) {
            boxRenderer.dispose();
            boxRenderer = null;
        }
        cameraDevice = null;
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    public boolean start()
    {
        boolean status = true;
        if (cameraDevice != null) {
            status &= cameraDevice.start(new CameraCallback());
        } else {
            status = false;
        }
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public void stop()
    {
        if (cameraDevice != null) {
            cameraDevice.stop();
        }
        for (ImageTracker tracker : trackers) {
            tracker.stop();
        }
    }

    public void render(int width, int height, int screenRotation)
    {
        while (scheduler.runOne())
        {
        }

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        OutputFrame oframe = outputFrameBuffer.peek();
        if(oframe == null) { return; }
        InputFrame iframe = oframe.inputFrame();
        if(iframe == null) { oframe.dispose(); return; }
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
            bgRenderer.render(imageProjection);

            Matrix44F projectionMatrix = cameraParameters.projection(0.01f, 1000.f, viewport_aspect_ratio, screenRotation, true, false);
            for (FrameFilterResult oResult : oframe.results()) {
                ImageTrackerResult result = (ImageTrackerResult)oResult;
                if (result != null) {
                    for (TargetInstance targetInstance : result.targetInstances()) {
                        int status = targetInstance.status();
                        if (status == TargetStatus.Tracked) {
                            Target target = targetInstance.target();
                            ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                            if (imagetarget == null) {
                                continue;
                            }
                            ArrayList<Image> images = ((ImageTarget) target).images();
                            Image targetImg = images.get(0);
                            float targetScale = imagetarget.scale();
                            Vec2F scale = new Vec2F(targetScale, targetScale * targetImg.height() / targetImg.width());
                            boxRenderer.render(projectionMatrix, targetInstance.pose(), scale);
                            for (Image img : images) {
                                img.dispose();
                            }
                        }
                    }
                    result.dispose();
                }
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

    class CameraCallback implements Camera.PreviewCallback{

        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera)
        {
            Buffer buf = null;
            Image img = null;
            InputFrame frame = null;
            boolean success = false;
            try {
                CameraParameters cameraParameters = cameraDevice.getmCameraParameters();
                buf = Buffer.wrapByteArray(data, 0, data.length, true, new FunctorOfVoid() {
                    @Override
                    public void invoke() {
                        camera.addCallbackBuffer(data);
                    }
                });

                img = new Image(buf,
                        cameraDevice.getPixelFormat(),
                        cameraParameters.size().data[0],
                        cameraParameters.size().data[1]);

                frame = InputFrame.createWithImageAndCameraParametersAndTemporal(img, cameraParameters, SystemClock.elapsedRealtimeNanos() * 1e-9);
                sink.handle(frame);
                success = true;
            } finally {
                if(buf != null) buf.dispose();
                if(img != null) img.dispose();
                if(frame != null) frame.dispose();
                if (!success) {
                    camera.addCallbackBuffer(data);
                }
            }
        }
    }
}
