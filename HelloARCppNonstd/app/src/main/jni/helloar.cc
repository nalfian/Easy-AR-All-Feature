//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

#include "helloar.hpp"

#include "renderer.hpp"
#include "boxrenderer.hpp"

#include "easyar/callbackscheduler.hxx"
#include "easyar/matrix.hxx"
#include "easyar/vector.hxx"
#include "easyar/camera.hxx"
#include "easyar/imagetracker.hxx"
#include "easyar/imagetarget.hxx"

#include <math.h>
#include <string.h>
#include <GLES2/gl2.h>
#include <android/log.h>

easyar::DelayedCallbackScheduler * scheduler = NULL;
easyar::CameraDevice * camera = NULL;
easyar::ImageTracker * tracker = NULL;
easyar::InputFrameThrottler * throttler = NULL;
easyar::OutputFrameFork * outputFrameFork = NULL;
easyar::OutputFrameBuffer * outputFrameBuffer = NULL;
easyar::InputFrameToFeedbackFrameAdapter * i2FAdapter = NULL;
Renderer * bgRenderer = NULL;
BoxRenderer * boxRenderer = NULL;
int previousInputFrameIndex = -1;

void loadTargetCallback(void * _state, easyar::Target * target, bool status)
{
    if (target == NULL) { return; }

    easyar::String * name = NULL;
    target->name(&name);
    __android_log_print(ANDROID_LOG_INFO, "HelloAR", "load target (%d): %s (%d)", status, name->begin(), target->runtimeID());
    delete name;
}

void loadFromImage(easyar::ImageTracker * tracker, const char * path, const char * name)
{
    easyar::ImageTarget * imageTarget = NULL;
    easyar::String * sPath = NULL;
    easyar::String::from_utf8_begin(path, &sPath);
    easyar::String * sName = NULL;
    easyar::String::from_utf8_begin(name, &sName);
    easyar::String * sEmpty = NULL;
    easyar::String::from_utf8_begin("", &sEmpty);
    easyar::ImageTarget::createFromImageFile(sPath, easyar::StorageType_Assets, sName, sEmpty, sEmpty, 1.0f, &imageTarget);
    if (imageTarget != NULL) {
        tracker->loadTarget(imageTarget, scheduler, easyar::FunctorOfVoidFromTargetAndBool(NULL, loadTargetCallback, NULL));
    }
    delete imageTarget;
    delete sEmpty;
    delete sName;
    delete sPath;
}

void recreate_context()
{
    if (bgRenderer != NULL) {
        delete bgRenderer;
        bgRenderer = NULL;
    }
    if (boxRenderer != NULL) {
        delete boxRenderer;
        boxRenderer = NULL;
    }
    previousInputFrameIndex = -1;
    bgRenderer = new Renderer();
    boxRenderer = new BoxRenderer();
}

void initialize()
{
    do {
        scheduler = new easyar::DelayedCallbackScheduler();

        recreate_context();

        easyar::InputFrameThrottler::create(&throttler);
        easyar::InputFrameToFeedbackFrameAdapter::create(&i2FAdapter);
        easyar::OutputFrameFork::create(2, &outputFrameFork);
        easyar::OutputFrameBuffer::create(&outputFrameBuffer);

        easyar::CameraDeviceSelector::createCameraDevice(easyar::CameraDevicePreference_PreferObjectSensing, &camera);
        if (!camera->openWithPreferredType(easyar::CameraDeviceType_Back)){ break; }
        camera->setFocusMode(easyar::CameraDeviceFocusMode_Continousauto);
        camera->setSize(easyar::Vec2I(1280, 960));
        camera->setBufferCapacity(10);

        easyar::ImageTracker::create(&tracker);
        loadFromImage(tracker, "sightplus/argame00.jpg", "argame00");
        loadFromImage(tracker, "sightplus/argame01.jpg", "argame01");
        loadFromImage(tracker, "sightplus/argame02.jpg", "argame02");
        loadFromImage(tracker, "sightplus/argame03.jpg", "argame03");
        loadFromImage(tracker, "idback.jpg", "idback");
        loadFromImage(tracker, "namecard.jpg", "namecard");

        easyar::InputFrameSource * camera_inputFrameSource;
        easyar::InputFrameSink * throttler_input;
        easyar::InputFrameSource * throttler_output;
        easyar::InputFrameSink * i2FAdapter_input;
        easyar::FeedbackFrameSource * i2FAdapter_output;
        easyar::FeedbackFrameSink * tracker_feedbackFrameSink;
        easyar::OutputFrameSource * tracker_outputFrameSource;
        easyar::OutputFrameSink * outputFrameFork_input;
        easyar::OutputFrameSource * outputFrameFork_output_0;
        easyar::OutputFrameSink * outputFrameBuffer_input;
        easyar::SignalSource * outputFrameBuffer_signalOutput;
        easyar::SignalSink * throttler_signalInput;
        easyar::OutputFrameSource * outputFrameFork_output_1;
        easyar::OutputFrameSink * i2FAdapter_sideInput;

        camera->inputFrameSource(&camera_inputFrameSource);
        throttler->input(&throttler_input);
        throttler->output(&throttler_output);
        i2FAdapter->input(&i2FAdapter_input);
        i2FAdapter->output(&i2FAdapter_output);
        tracker->feedbackFrameSink(&tracker_feedbackFrameSink);
        tracker->outputFrameSource(&tracker_outputFrameSource);
        outputFrameFork->input(&outputFrameFork_input);
        outputFrameFork->output(0, &outputFrameFork_output_0);
        outputFrameBuffer->input(&outputFrameBuffer_input);
        outputFrameBuffer->signalOutput(&outputFrameBuffer_signalOutput);
        throttler->signalInput(&throttler_signalInput);
        outputFrameFork->output(1, &outputFrameFork_output_1);
        i2FAdapter->sideInput(&i2FAdapter_sideInput);

        camera_inputFrameSource->connect(throttler_input);
        throttler_output->connect(i2FAdapter_input);
        i2FAdapter_output->connect(tracker_feedbackFrameSink);
        tracker_outputFrameSource->connect(outputFrameFork_input);
        outputFrameFork_output_0->connect(outputFrameBuffer_input);

        outputFrameBuffer_signalOutput->connect(throttler_signalInput);
        outputFrameFork_output_1->connect(i2FAdapter_sideInput);

        //CameraDevice and rendering each require an additional buffer
        camera->setBufferCapacity(throttler->bufferRequirement() + i2FAdapter->bufferRequirement() + outputFrameBuffer->bufferRequirement() + tracker->bufferRequirement() + 2);

        delete camera_inputFrameSource;
        delete throttler_input;
        delete throttler_output;
        delete i2FAdapter_input;
        delete i2FAdapter_output;
        delete tracker_feedbackFrameSink;
        delete tracker_outputFrameSource;
        delete outputFrameFork_input;
        delete outputFrameFork_output_0;
        delete outputFrameBuffer_input;
        delete outputFrameBuffer_signalOutput;
        delete throttler_signalInput;
        delete outputFrameFork_output_1;
        delete i2FAdapter_sideInput;

        return;
    } while (0);

    delete tracker;
    tracker = NULL;
    delete camera;
    camera = NULL;
    delete outputFrameBuffer;
    outputFrameBuffer = NULL;
    delete outputFrameFork;
    outputFrameFork = NULL;
    delete i2FAdapter;
    i2FAdapter = NULL;
}

void finalize()
{
    delete boxRenderer;
    boxRenderer = NULL;
    delete bgRenderer;
    bgRenderer = NULL;
    delete tracker;
    tracker = NULL;
    delete camera;
    camera = NULL;
    delete outputFrameBuffer;
    outputFrameBuffer = NULL;
    delete outputFrameFork;
    outputFrameFork = NULL;
    delete i2FAdapter;
    i2FAdapter = NULL;
}

bool start()
{
    bool status = true;
    if (camera != NULL) {
        status &= camera->start();
    } else {
        status = false;
    }
    if (tracker != NULL) {
        status &= tracker->start();
    } else {
        status = false;
    }
    return status;
}

void stop()
{
    if (tracker != NULL) {
        tracker->stop();
    }
    if (camera != NULL) {
        camera->stop();
    }
}

void render(int width, int height, int screenRotation)
{
    while (scheduler->runOne())
    {
    };

    glViewport(0, 0, width, height);
    glClearColor(0.f, 0.f, 0.f, 1.f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    easyar::OutputFrame * frame = NULL;
    easyar::InputFrame * inputFrame = NULL;
    easyar::CameraParameters * cameraParameters = NULL;
    easyar::Image * image = NULL;
    easyar::Buffer * buffer = NULL;
    easyar::ListOfOptionalOfFrameFilterResult * results = NULL;

    do {
        outputFrameBuffer->peek(&frame);
        if (frame == NULL) { break; }
        frame->inputFrame(&inputFrame);
        if (inputFrame == NULL) { break; }
        if (!inputFrame->hasCameraParameters()) { break; }
        inputFrame->cameraParameters(&cameraParameters);
        float viewport_aspect_ratio = (float)width / (float)height;
        easyar::Matrix44F projection = cameraParameters->projection(0.01f, 1000.f, viewport_aspect_ratio, screenRotation, true, false);
        easyar::Matrix44F imageProjection = cameraParameters->imageProjection(viewport_aspect_ratio, screenRotation, true, false);
        inputFrame->image(&image);
        if (inputFrame->index() != previousInputFrameIndex) {
            image->buffer(&buffer);
            bgRenderer->upload(image->format(), image->width(), image->height(), buffer->data());
            previousInputFrameIndex = inputFrame->index();
        }
        bgRenderer->render(imageProjection);

        frame->results(&results);

        int resultSize = results->size();
        for (int i = 0; i < resultSize; i += 1) {
            easyar::FrameFilterResult * result = results->at(i);
            if (result == NULL){ continue; }
            easyar::ImageTrackerResult * imageTrackerResult = NULL;
            easyar::ImageTrackerResult::tryCastFromFrameFilterResult(result, &imageTrackerResult);
            if (imageTrackerResult == NULL){ continue; }
            easyar::ListOfTargetInstance * targetInstances = NULL;
            imageTrackerResult->targetInstances(&targetInstances);
            int targetInstanceSize = targetInstances->size();
            for (int k = 0; k < targetInstanceSize; k += 1) {
                easyar::TargetInstance * targetInstance = targetInstances->at(k);
                easyar::TargetStatus status = targetInstance->status();

                if (status == easyar::TargetStatus_Tracked) {
                    easyar::Target * target = NULL;
                    easyar::ImageTarget * imagetarget = NULL;
                    targetInstance->target(&target);
                    easyar::ImageTarget::tryCastFromTarget(target, &imagetarget);
                    if (imagetarget != NULL) {
                        easyar::Vec2F targetSize(imagetarget->scale(), imagetarget->scale() / imagetarget->aspectRatio());
                        boxRenderer->render(projection, targetInstance->pose(), targetSize);
                    }
                    delete target;
                    target = NULL;
                    delete imagetarget;
                    imagetarget = NULL;
                }
            }
            delete imageTrackerResult;
            imageTrackerResult = NULL;
        }
        break;
    } while (0);

    delete frame;
    frame = NULL;
    delete inputFrame;
    inputFrame = NULL;
    delete cameraParameters;
    cameraParameters = NULL;
    delete image;
    image = NULL;
    delete buffer;
    buffer = NULL;
    delete results;
    results = NULL;
}
