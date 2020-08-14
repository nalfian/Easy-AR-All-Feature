//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import java.nio.ByteBuffer
import java.util.ArrayList
import android.opengl.GLES20
import android.util.Log

import cn.easyar.*

class HelloAR {
    private var scheduler: DelayedCallbackScheduler? = null
    private var camera: CameraDevice? = null
    private val trackers: ArrayList<ImageTracker>
    private var bgRenderer: BGRenderer? = null
    private var boxRenderer: BoxRenderer? = null
    private var throttler: InputFrameThrottler? = null
    private var feedbackFrameFork: FeedbackFrameFork? = null
    private var i2OAdapter: InputFrameToOutputFrameAdapter? = null
    private var inputFrameFork: InputFrameFork? = null
    private var join: OutputFrameJoin? = null
    private var oFrameBuffer: OutputFrameBuffer? = null
    private var i2FAdapter: InputFrameToFeedbackFrameAdapter? = null
    private var outputFrameFork: OutputFrameFork? = null
    private var previousInputFrameIndex = -1
    private var imageBytes: ByteArray? = null

    init {
        scheduler = DelayedCallbackScheduler()
        trackers = ArrayList()
    }

    private fun loadFromImage(tracker: ImageTracker, path: String, name: String) {
        val target = ImageTarget.createFromImageFile(path, StorageType.Assets, name, "", "", 1.0f)
        if (target == null) {
            Log.e("HelloAR", "target create failed or key is not correct")
            return
        }
        tracker.loadTarget(target, scheduler!!) { t, status -> Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, t.name(), t.runtimeID())) }
        target.dispose()
    }

    fun recreate_context() {
        if (bgRenderer != null) {
            bgRenderer!!.dispose()
            bgRenderer = null
        }
        if (boxRenderer != null) {
            boxRenderer!!.dispose()
            boxRenderer = null
        }
        previousInputFrameIndex = -1
        bgRenderer = BGRenderer()
        boxRenderer = BoxRenderer()
    }

    fun initialize() {
        recreate_context()

        camera = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing)
        throttler = InputFrameThrottler.create()
        inputFrameFork = InputFrameFork.create(2)
        join = OutputFrameJoin.create(2)
        oFrameBuffer = OutputFrameBuffer.create()
        i2OAdapter = InputFrameToOutputFrameAdapter.create()
        i2FAdapter = InputFrameToFeedbackFrameAdapter.create()
        outputFrameFork = OutputFrameFork.create(2)

        var status = true
        status = status and camera!!.openWithPreferredType(CameraDeviceType.Back)
        camera!!.setSize(Vec2I(1280, 960))
        camera!!.setFocusMode(CameraDeviceFocusMode.Continousauto)
        if (!status) {
            return
        }
        val tracker = ImageTracker.create()
        loadFromImage(tracker, "sightplus/argame00.jpg", "argame00")
        loadFromImage(tracker, "sightplus/argame01.jpg", "argame01")
        loadFromImage(tracker, "sightplus/argame02.jpg", "argame02")
        loadFromImage(tracker, "sightplus/argame03.jpg", "argame03")
        loadFromImage(tracker, "idback.jpg", "idback")
        loadFromImage(tracker, "namecard.jpg", "namecard")
        tracker.setSimultaneousNum(6)
        trackers.add(tracker)

        feedbackFrameFork = FeedbackFrameFork.create(trackers.size)

        camera!!.inputFrameSource().connect(throttler!!.input())
        throttler!!.output().connect(inputFrameFork!!.input())
        inputFrameFork!!.output(0).connect(i2OAdapter!!.input())
        i2OAdapter!!.output().connect(join!!.input(0))

        inputFrameFork!!.output(1).connect(i2FAdapter!!.input())
        i2FAdapter!!.output().connect(feedbackFrameFork!!.input())
        var k = 0
        var trackerBufferRequirement = 0
        for (_tracker in trackers) {
            feedbackFrameFork!!.output(k).connect(_tracker.feedbackFrameSink())
            _tracker.outputFrameSource().connect(join!!.input(k + 1))
            trackerBufferRequirement += _tracker.bufferRequirement()
            k++
        }

        join!!.output().connect(outputFrameFork!!.input())
        outputFrameFork!!.output(0).connect(oFrameBuffer!!.input())
        outputFrameFork!!.output(1).connect(i2FAdapter!!.sideInput())
        oFrameBuffer!!.signalOutput().connect(throttler!!.signalInput())

        //CameraDevice and rendering each require an additional buffer
        camera!!.setBufferCapacity(throttler!!.bufferRequirement() + i2FAdapter!!.bufferRequirement() + oFrameBuffer!!.bufferRequirement() + trackerBufferRequirement + 2)
    }

    fun dispose() {
        for (tracker in trackers) {
            tracker.dispose()
        }
        trackers.clear()
        if (bgRenderer != null) {
            bgRenderer!!.dispose()
            bgRenderer = null
        }
        if (boxRenderer != null) {
            boxRenderer!!.dispose()
            boxRenderer = null
        }
        if (camera != null) {
            camera!!.dispose()
            camera = null
        }
        if (scheduler != null) {
            scheduler!!.dispose()
            scheduler = null
        }
    }

    fun start(): Boolean {
        var status = true
        if (camera != null) {
            status = status and camera!!.start()
        } else {
            status = false
        }
        for (tracker in trackers) {
            status = status and tracker.start()
        }
        return status
    }

    fun stop() {
        if (camera != null) {
            camera!!.stop()
        }
        for (tracker in trackers) {
            tracker.stop()
        }
    }

    fun render(width: Int, height: Int, screenRotation: Int) {
        while (scheduler!!.runOne()) {
        }

        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val oframe = oFrameBuffer!!.peek() ?: return
        val iframe = oframe.inputFrame()
        val cameraParameters = iframe.cameraParameters()
        val viewport_aspect_ratio = width.toFloat() / height.toFloat()
        val imageProjection = cameraParameters.imageProjection(viewport_aspect_ratio, screenRotation, true, false)
        val image = iframe.image()

        try {
            if (iframe.index() != previousInputFrameIndex) {
                val buffer = image.buffer()
                try {
                    if ((imageBytes == null) || (imageBytes!!.size != buffer.size())) {
                        imageBytes = ByteArray(buffer.size())
                    }
                    buffer.copyToByteArray(imageBytes!!)
                    bgRenderer!!.upload(image.format(), image.width(), image.height(), ByteBuffer.wrap(imageBytes))
                } finally {
                    buffer.dispose()
                }
                previousInputFrameIndex = iframe.index()
            }
            bgRenderer!!.render(imageProjection)

            val projectionMatrix = cameraParameters.projection(0.01f, 1000f, viewport_aspect_ratio, screenRotation, true, false)
            for (oResult in oframe.results()) {
                val result = oResult as? ImageTrackerResult
                if (result != null) {
                    for (targetInstance in result.targetInstances()) {
                        val status = targetInstance.status()
                        if (status == TargetStatus.Tracked) {
                            val target = targetInstance.target()
                            val imagetarget = (if (target is ImageTarget) target else null)
                                    ?: continue
                            val images = (target as ImageTarget).images()
                            val targetImg = images[0]
                            val targetScale = imagetarget.scale()
                            val scale = Vec2F(targetScale, targetScale * targetImg.height() / targetImg.width())
                            boxRenderer!!.render(projectionMatrix, targetInstance.pose(), scale)
                            for (img in images) {
                                img.dispose()
                            }
                        }
                    }
                    result.dispose()
                }
            }
        } finally {
            iframe.dispose()
            oframe.dispose()
            cameraParameters.dispose()
            image.dispose()
        }
    }
}
