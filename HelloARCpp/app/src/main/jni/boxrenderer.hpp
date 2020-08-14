//=============================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//=============================================================================================================================

#pragma once

#include <easyar/types.hpp>

typedef void *EGLContext;

// all methods of this class can only be called on one thread with the same OpenGLES
class BoxRenderer
{
public:
    BoxRenderer();
    ~BoxRenderer();
    void render(const easyar::Matrix44F& projectionMatrix, const easyar::Matrix44F& cameraview, easyar::Vec2F size);
private:
    EGLContext current_context;
    unsigned int program_box;
    int pos_coord_box;
    int pos_color_box;
    int pos_trans_box;
    int pos_proj_box;
    unsigned int vbo_coord_box;
    unsigned int vbo_color_box;
    unsigned int vbo_color_box_2;
    unsigned int vbo_faces_box;
};
