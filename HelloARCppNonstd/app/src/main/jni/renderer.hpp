//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

#pragma once

#include "easyar/matrix.hxx"
#include "easyar/image.hxx"

class GLES20Renderer;

// all methods of this class can only be called on one thread with the same OpenGLES
class Renderer
{
public:
    Renderer();

    void upload(easyar::PixelFormat format, int width, int height, void * bufferData);
    void render(easyar::Matrix44F imageProjection);
public:
    ~Renderer();
private:
    GLES20Renderer * impl_;
};
