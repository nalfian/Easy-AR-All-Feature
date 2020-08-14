//=============================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//=============================================================================================================================

#include "helloar.hpp"

#include "renderer.hpp"
#include "boxrenderer.hpp"

#include <easyar/types.hpp>

#include <android/log.h>
#include <GLES2/gl2.h>

#include <cmath>
#include <string>
#include <algorithm>
#include <memory>
#include <unordered_map>

std::shared_ptr<easyar::DelayedCallbackScheduler> scheduler;
std::shared_ptr<easyar::CameraDevice> camera;
std::shared_ptr<easyar::ImageTracker> tracker;
std::shared_ptr<easyar::InputFrameThrottler> throttler;
std::shared_ptr<easyar::OutputFrameFork> outputFrameFork;
std::shared_ptr<easyar::OutputFrameBuffer> outputFrameBuffer;
std::shared_ptr<easyar::InputFrameToFeedbackFrameAdapter> i2FAdapter;
std::shared_ptr<Renderer> bgRenderer;
std::shared_ptr<BoxRenderer> boxRenderer;
int previousInputFrameIndex = -1;

void loadFromImage(std::shared_ptr<easyar::ImageTracker> tracker, const std::string& path,  const std::string& name)
{
    auto imageTarget = easyar::ImageTarget::createFromImageFile(path, easyar::StorageType::Assets, name, "", "", 1.0f);
    if (imageTarget.has_value())
    {
        tracker->loadTarget(imageTarget.value(), scheduler, [](std::shared_ptr<easyar::Target> target, bool status) {
            __android_log_print(ANDROID_LOG_INFO, "HelloAR", "load target (%d): %s (%d)", status, target->name().c_str(), target->runtimeID());
        });
    }
}

void recreate_context()
{
    bgRenderer = nullptr;
    boxRenderer = nullptr;
    previousInputFrameIndex = -1;
    bgRenderer = std::make_shared<Renderer>();
    boxRenderer = std::make_shared<BoxRenderer>();
}

void initialize()
{
    scheduler = std::make_shared<easyar::DelayedCallbackScheduler>();

    recreate_context();

    throttler = easyar::InputFrameThrottler::create();
    i2FAdapter = easyar::InputFrameToFeedbackFrameAdapter::create();
    outputFrameFork = easyar::OutputFrameFork::create(2);
    outputFrameBuffer = easyar::OutputFrameBuffer::create();

    camera = easyar::CameraDeviceSelector::createCameraDevice(easyar::CameraDevicePreference::PreferObjectSensing);
    if (!camera->openWithPreferredType(easyar::CameraDeviceType::Back)) { return; }
    camera->setFocusMode(easyar::CameraDeviceFocusMode::Continousauto);
    camera->setSize(easyar::Vec2I{{1280, 960}});

    tracker = easyar::ImageTracker::create();
    loadFromImage(tracker, "sightplus/argame00.jpg", "argame00");
    loadFromImage(tracker, "sightplus/argame01.jpg", "argame01");
    loadFromImage(tracker, "sightplus/argame02.jpg", "argame02");
    loadFromImage(tracker, "sightplus/argame03.jpg", "argame03");
    loadFromImage(tracker, "idback.jpg", "idback");
    loadFromImage(tracker, "namecard.jpg", "namecard");

    camera->inputFrameSource()->connect(throttler->input());
    throttler->output()->connect(i2FAdapter->input());
    i2FAdapter->output()->connect(tracker->feedbackFrameSink());
    tracker->outputFrameSource()->connect(outputFrameFork->input());
    outputFrameFork->output(0)->connect(outputFrameBuffer->input());

    outputFrameBuffer->signalOutput()->connect(throttler->signalInput());
    outputFrameFork->output(1)->connect(i2FAdapter->sideInput());

    //CameraDevice and rendering each require an additional buffer
    camera->setBufferCapacity(throttler->bufferRequirement() + i2FAdapter->bufferRequirement() + outputFrameBuffer->bufferRequirement() + tracker->bufferRequirement() + 2);
}

void finalize()
{
    tracker = nullptr;
    bgRenderer = nullptr;
    boxRenderer = nullptr;
    camera = nullptr;
    throttler = nullptr;
    outputFrameFork = nullptr;
    outputFrameBuffer = nullptr;
    i2FAdapter = nullptr;
    scheduler = nullptr;
}

bool start()
{
    bool status = true;
    if (camera != nullptr)
    {
        status &= camera->start();
    }
    else
    {
        status = false;
    }
    if (tracker != nullptr)
    {
        status &= tracker->start();
    }
    else
    {
        status = false;
    }
    return status;
}

void stop()
{
    if (camera != nullptr)
    {
        camera->stop();
    }
    if (tracker != nullptr)
    {
        tracker->stop();
    }
}

void render(int width, int height, int screenRotation)
{
    while (scheduler->runOne())
    {
    }

    glViewport(0, 0, width, height);
    glClearColor(0.f, 0.f, 0.f, 1.f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    auto oFrame = outputFrameBuffer->peek();
    if (!oFrame.has_value()) { return; }
    auto frame = oFrame.value();
    if (!frame->inputFrame()->hasCameraParameters()) { return; }
    auto cameraParameters = frame->inputFrame()->cameraParameters();
    float viewport_aspect_ratio = (float)width / (float)height;
    auto projection = cameraParameters->projection(0.01f, 1000.f, viewport_aspect_ratio, screenRotation, true, false);
    auto imageProjection = cameraParameters->imageProjection(viewport_aspect_ratio, screenRotation, true, false);
    auto image = frame->inputFrame()->image();
    if (frame->inputFrame()->index() != previousInputFrameIndex)
    {
        auto buffer = image->buffer();
        bgRenderer->upload(image->format(), image->width(), image->height(), buffer->data());
        previousInputFrameIndex = frame->inputFrame()->index();
    }
    bgRenderer->render(imageProjection);
    for (auto && result : frame->results())
    {
       if (!result.has_value()) { continue; }
       auto imageTrackerResult = std::dynamic_pointer_cast<easyar::ImageTrackerResult>(result.value());
       if (imageTrackerResult != nullptr)
       {
           for (auto && targetInstance : imageTrackerResult->targetInstances())
           {
               auto status = targetInstance->status();
               auto target = targetInstance->target();
               if (!target.has_value())
               {
                   continue;
               }
               auto imagetarget = std::dynamic_pointer_cast<easyar::ImageTarget>(target.value());
               if (imagetarget == nullptr)
               {
                   continue;
               }
               if (status == easyar::TargetStatus::Tracked)
               {
                   easyar::Vec2F targetSize{{imagetarget->scale(), imagetarget->scale() / imagetarget->aspectRatio()}};
                   boxRenderer->render(projection, targetInstance->pose(), targetSize);
               }
           }
       }
    }
}
