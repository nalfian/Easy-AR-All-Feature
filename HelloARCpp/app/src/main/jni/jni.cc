//=============================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//=============================================================================================================================

#include "helloar.hpp"

#include <exception>
#include <typeinfo>

#include <jni.h>
#include <android/log.h>

extern "C" {

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1initialize(JNIEnv*, jobject)
{
    try {
        initialize();
        start();
    } catch (std::exception &ex) {
        __android_log_print(ANDROID_LOG_ERROR, "HelloAR", "%s: %s", typeid(*(&ex)).name(), ex.what());
    }
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1finalize(JNIEnv*, jobject)
{
    try {
        stop();
        finalize();
    } catch (std::exception &ex) {
        __android_log_print(ANDROID_LOG_ERROR, "HelloAR", "%s: %s", typeid(*(&ex)).name(), ex.what());
    }
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1render(JNIEnv*, jobject, jint width, jint height, jint screenRotation)
{
    try {
        render(width, height, screenRotation);
    } catch (std::exception &ex) {
        __android_log_print(ANDROID_LOG_ERROR, "HelloAR", "%s: %s", typeid(*(&ex)).name(), ex.what());
    }
}

JNIEXPORT void JNICALL Java_cn_easyar_samples_helloar_GLView_helloar_1recreate_1context(JNIEnv*, jobject)
{
    try {
        recreate_context();
    } catch (std::exception &ex) {
        __android_log_print(ANDROID_LOG_ERROR, "HelloAR", "%s: %s", typeid(*(&ex)).name(), ex.what());
    }
}

}
