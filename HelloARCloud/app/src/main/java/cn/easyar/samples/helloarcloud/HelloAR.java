//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloarcloud;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import android.opengl.GLES20;
import android.util.Log;

import cn.easyar.Buffer;
import cn.easyar.CameraDevicePreference;
import cn.easyar.CameraDeviceSelector;
import cn.easyar.CameraParameters;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CloudRecognizationResult;
import cn.easyar.CloudRecognizationStatus;
import cn.easyar.CloudRecognizer;
import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FunctorOfVoidFromCloudRecognizationResult;
import cn.easyar.FunctorOfVoidFromTargetAndBool;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.ImageTrackerResult;
import cn.easyar.Matrix44F;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec2F;
import cn.easyar.InputFrameFork;
import cn.easyar.InputFrameThrottler;
import cn.easyar.InputFrameToFeedbackFrameAdapter;
import cn.easyar.InputFrameToOutputFrameAdapter;
import cn.easyar.OutputFrame;
import cn.easyar.InputFrame;
import cn.easyar.OutputFrameBuffer;
import cn.easyar.OutputFrameJoin;
import cn.easyar.FrameFilterResult;
import cn.easyar.Image;
import cn.easyar.FeedbackFrameFork;
import cn.easyar.OutputFrameFork;

public class HelloAR
{
    private DelayedCallbackScheduler scheduler;
    private CameraDevice camera;
    private ArrayList<ImageTracker> trackers;
    private BGRenderer bgRenderer;
    private BoxRenderer boxRenderer;
    private CloudRecognizer cloud_recognizer;
    private InputFrameThrottler throttler;
    private FeedbackFrameFork feedbackFrameFork;
    private InputFrameToOutputFrameAdapter i2OAdapter;
    private InputFrameFork inputFrameFork;
    private OutputFrameJoin join;
    private OutputFrameBuffer oFrameBuffer;
    private InputFrameToFeedbackFrameAdapter i2FAdapter;
    private OutputFrameFork outputFrameFork;
    private int previousInputFrameIndex = -1;
    private byte[] imageBytes = null;
    private long previousResolveTime = System.nanoTime();
    private boolean resolveStanding = false;

    public HelloAR()
    {
        scheduler = new DelayedCallbackScheduler();
        trackers = new ArrayList<ImageTracker>();
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

    public void initialize(final String cloudRecognitionServiceServerAddress, String apiKey, String apiSecret, String cloudRecognitionServiceAppId)
    {
        recreate_context();

        camera = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing);
        throttler = InputFrameThrottler.create();
        inputFrameFork = InputFrameFork.create(2);
        join = OutputFrameJoin.create(2);
        oFrameBuffer = OutputFrameBuffer.create();
        i2OAdapter = InputFrameToOutputFrameAdapter.create();
        i2FAdapter = InputFrameToFeedbackFrameAdapter.create();
        outputFrameFork = OutputFrameFork.create(2);

        boolean status = true;
        status &= camera.openWithPreferredType(CameraDeviceType.Back);
        camera.setSize(new Vec2I(1280, 960));
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        if (!status) { return; }
        cloud_recognizer = CloudRecognizer.create(cloudRecognitionServiceServerAddress, apiKey, apiSecret, cloudRecognitionServiceAppId);

        ImageTracker tracker = ImageTracker.create();
        trackers.add(tracker);
        feedbackFrameFork = FeedbackFrameFork.create(trackers.size());

        camera.inputFrameSource().connect(throttler.input());
        throttler.output().connect(inputFrameFork.input());
        inputFrameFork.output(0).connect(i2OAdapter.input());
        i2OAdapter.output().connect(join.input(0));

        inputFrameFork.output(1).connect(i2FAdapter.input());
        i2FAdapter.output().connect(feedbackFrameFork.input());
        int k = 0;
        int trackerBufferRequirement = 0;
        for (ImageTracker _tracker : trackers)
        {
            feedbackFrameFork.output(k).connect(_tracker.feedbackFrameSink());
            _tracker.outputFrameSource().connect(join.input(k+1));
            trackerBufferRequirement += _tracker.bufferRequirement();
            k++;
        }

        join.output().connect(outputFrameFork.input());
        outputFrameFork.output(0).connect(oFrameBuffer.input());
        outputFrameFork.output(1).connect(i2FAdapter.sideInput());
        oFrameBuffer.signalOutput().connect(throttler.signalInput());

        //CameraDevice and rendering each require an additional buffer
        camera.setBufferCapacity(throttler.bufferRequirement() + i2FAdapter.bufferRequirement() + oFrameBuffer.bufferRequirement() + trackerBufferRequirement + 2);
    }

    public void dispose()
    {
        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        if (cloud_recognizer != null) {
            cloud_recognizer.dispose();
            cloud_recognizer = null;
        }
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (boxRenderer != null) {
            boxRenderer.dispose();
            boxRenderer = null;
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
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public void stop()
    {
        if (camera != null) {
            camera.stop();
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

        OutputFrame oframe = oFrameBuffer.peek();
        if (oframe == null) { return; }
        InputFrame iframe = oframe.inputFrame();
        if (iframe == null) { oframe.dispose(); return; }
        CameraParameters cameraParameters = iframe.cameraParameters();
        if (cameraParameters == null) { oframe.dispose(); iframe.dispose(); return; }

        long now = System.nanoTime();
        if (now >= previousResolveTime + 1000000000) {
            boolean resolveStandingLocal = resolveStanding;
            if (!resolveStandingLocal) {
                resolveStanding = true;
                previousResolveTime = now;
                cloud_recognizer.resolve(iframe, scheduler, new FunctorOfVoidFromCloudRecognizationResult() {
                    private HashSet<String> uids = new HashSet<String>();
                    @Override
                    public void invoke(CloudRecognizationResult result) {
                        try {
                            int status = result.getStatus();
                            if (status == CloudRecognizationStatus.UnknownError) {
                                Log.i("HelloAR", "CloudRecognizerCallBack: UnknownError: " + result.getUnknownErrorMessage());
                                return;
                            }
                            if (status == CloudRecognizationStatus.FoundTarget) {
                                Log.i("HelloAR", "CloudRecognizerCallBack: FoundTarget");
                            } else if (status == CloudRecognizationStatus.TargetNotFound) {
                                Log.i("HelloAR", "CloudRecognizerCallBack: TargetNotFound");
                                return;
                            } else if (status == CloudRecognizationStatus.ReachedAccessLimit) {
                                Log.i("HelloAR", "CloudRecognizerCallBack: ReachedAccessLimit");
                                return;
                            } else if (status == CloudRecognizationStatus.RequestIntervalTooLow) {
                                Log.i("HelloAR", "CloudRecognizerCallBack: RequestIntervalTooLow");
                                return;
                            } else {
                                throw new IllegalStateException();
                            }
                            ImageTarget t = result.getTarget();
                            if (!uids.contains(t.uid())) {
                                Log.i("HelloAR", "add cloud target: " + t.uid());
                                uids.add(t.uid());
                                trackers.get(0).loadTarget(t, scheduler, new FunctorOfVoidFromTargetAndBool() {
                                    @Override
                                    public void invoke(Target target, boolean status) {
                                        Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                                    }
                                });
                            }
                        } finally {
                            resolveStanding = false;
                        }
                    }
                });
            }
        }

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
}
