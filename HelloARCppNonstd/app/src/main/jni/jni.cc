//=============================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//=============================================================================================================================

#include "helloar.hpp"

#include <jni.h>
#include <android/log.h>

extern "C" {

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1initialize(JNIEnv*, jobject)
{
    initialize();
    start();
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1finalize(JNIEnv*, jobject)
{
    stop();
    finalize();
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1render(JNIEnv*, jobject, jint width, jint height, jint screenRotation)
{
    render(width, height, screenRotation);
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1recreate_1context(JNIEnv*, jobject)
{
    recreate_context();
}

}
