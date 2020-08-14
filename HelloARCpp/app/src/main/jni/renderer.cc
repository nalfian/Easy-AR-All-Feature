//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

#include "renderer.hpp"

#include "easyar/types.hpp"

#include <GLES2/gl2.h>
#include <EGL/egl.h>

using namespace easyar;

const char * videobackground_vert =
"attribute vec4 coord;\n"
"attribute vec2 texCoord;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    gl_Position = coord;\n"
"    texc = texCoord;\n"
"}\n"
;
const char* videobackground_bgr_frag =
"#ifdef GL_ES\n"
"precision mediump float;\n"
"#endif\n"
"uniform sampler2D texture;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    gl_FragColor = texture2D(texture, texc).bgra;\n"
"}\n"
;
const char* videobackground_rgb_frag =
"#ifdef GL_ES\n"
"precision mediump float;\n"
"#endif\n"
"uniform sampler2D texture;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    gl_FragColor = texture2D(texture, texc);\n"
"}\n"
;
const char* videobackground_yuv_i420_yv12_frag =
"#ifdef GL_ES\n"
"precision highp float;\n"
"#endif\n"
"uniform sampler2D texture;\n"
"uniform sampler2D u_texture;\n"
"uniform sampler2D v_texture;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    float cb = texture2D(u_texture, texc).r - 0.5;\n"
"    float cr = texture2D(v_texture, texc).r - 0.5;\n"
"    vec3 ycbcr = vec3(texture2D(texture, texc).r, cb, cr);\n"
"    vec3 rgb = mat3(1, 1, 1,\n"
"        0, -0.344, 1.772,\n"
"        1.402, -0.714, 0) * ycbcr;\n"
"    gl_FragColor = vec4(rgb, 1.0);\n"
"}\n"
;
const char* videobackground_yuv_nv12_frag =
"#ifdef GL_ES\n"
"precision highp float;\n"
"#endif\n"
"uniform sampler2D texture;\n"
"uniform sampler2D uv_texture;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    vec2 cbcr = texture2D(uv_texture, texc).ra - vec2(0.5, 0.5);\n"
"    vec3 ycbcr = vec3(texture2D(texture, texc).r, cbcr);\n"
"    vec3 rgb = mat3(1, 1, 1,\n"
"        0, -0.344, 1.772,\n"
"        1.402, -0.714, 0) * ycbcr;\n"
"    gl_FragColor = vec4(rgb, 1.0);\n"
"}\n"
;
const char* videobackground_yuv_nv21_frag =
"#ifdef GL_ES\n"
"precision highp float;\n"
"#endif\n"
"uniform sampler2D texture;\n"
"uniform sampler2D uv_texture;\n"
"varying vec2 texc;\n"
"\n"
"void main(void)\n"
"{\n"
"    vec2 cbcr = texture2D(uv_texture, texc).ar - vec2(0.5, 0.5);\n"
"    vec3 ycbcr = vec3(texture2D(texture, texc).r, cbcr);\n"
"    vec3 rgb = mat3(1, 1, 1,\n"
"        0, -0.344, 1.772,\n"
"        1.402, -0.714, 0) * ycbcr;\n"
"    gl_FragColor = vec4(rgb, 1.0);\n"
"}\n"
;

class AutoRelease
{
private:
    std::function<void()> releaseAction_;
public:
    AutoRelease(std::function<void()> releaseAction)
    {
        releaseAction_ = releaseAction;
    }

    void execute()
    {
        if (releaseAction_ != nullptr)
        {
            releaseAction_();
            releaseAction_ = nullptr;
        }
    }

    void suppress()
    {
        releaseAction_ = nullptr;
    }

    ~AutoRelease()
    {
        if (releaseAction_ != nullptr)
        {
            releaseAction_();
        }
    }
};

Vec4F mul(const Matrix44F & lhs, const Vec4F & rhs)
{
    Vec4F v = {};
    for (int i = 0; i < 4; i += 1)
    {
        for (int k = 0; k < 4; k += 1)
        {
            v.data[i] += lhs.data[i * 4 + k] * rhs.data[k];
        }
    }
    return v;
}

class GLES20Renderer final
{
public:
    GLES20Renderer();
    ~GLES20Renderer();
    void upload(PixelFormat format, int width, int height, void* bufferData);
    void render(Matrix44F imageProjection);
private:
    void retrieveFrame(PixelFormat format, int width, int height, void* bufferData, int retrieve_count);

    bool initialize(PixelFormat format);
    void finalize(PixelFormat format);

    enum class FrameShader
    {
        RGB,
        YUV,
    };
    FrameShader background_shader_ = FrameShader::RGB;

    bool initialized_{false};
    EGLContext current_context_{};
    unsigned int background_program_{};
    unsigned int background_texture_id_{};
    unsigned int background_texture_uv_id_{};
    unsigned int background_texture_u_id_{};
    unsigned int background_texture_v_id_{};
    int background_coord_location_{-1};
    int background_texture_location_{-1};
    unsigned int background_coord_vbo_{};
    unsigned int background_texture_vbo_{};
    unsigned int background_texture_fbo_{};

    enum class RetrieveStatus
    {
        Unset,
        Upload,
        Clear,
    };

    PixelFormat current_format_{PixelFormat::Unknown};
    Vec2I current_image_size_;
};

static unsigned char yuv_black[24]{
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    0, 0, 0, 0,
    127, 127,
    127, 127,
    127, 127,
    127, 127,
};

GLES20Renderer::GLES20Renderer()
{
    current_context_ = eglGetCurrentContext();
}

GLES20Renderer::~GLES20Renderer()
{
    finalize(current_format_);
}

void GLES20Renderer::upload(PixelFormat format, int width, int height, void* bufferData)
{
    GLint bak_tex, bak_program, bak_active_tex, bak_tex_1, bak_tex_2;
    glGetIntegerv(GL_CURRENT_PROGRAM, &bak_program);
    glGetIntegerv(GL_ACTIVE_TEXTURE, &bak_active_tex);
    glActiveTexture(GL_TEXTURE0);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex);
    glActiveTexture(GL_TEXTURE1);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex_1);
    glActiveTexture(GL_TEXTURE2);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex_2);

    AutoRelease gl_restorer([&]()
    {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, bak_tex);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, bak_tex_1);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, bak_tex_2);
        glActiveTexture(bak_active_tex);
        glUseProgram(bak_program);
    });

    if (current_format_ != format)
    {
        finalize(current_format_);
        if (!initialize(format)) { return; }
        current_format_ = format;
    }
    current_image_size_ = {{width, height}};

    switch (background_shader_)
    {
        case FrameShader::RGB:
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, background_texture_id_);
            retrieveFrame(format, width, height, bufferData, 0);
            break;
        case FrameShader::YUV:
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, background_texture_id_);
            retrieveFrame(format, width, height, bufferData, 0);
            if (format == PixelFormat::YUV_NV21 || format == PixelFormat::YUV_NV12)
            {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, background_texture_uv_id_);
                retrieveFrame(format, width, height, bufferData, 1);
            }
            else
            {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, background_texture_u_id_);
                retrieveFrame(format, width, height, bufferData, 1);
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, background_texture_v_id_);
                retrieveFrame(format, width, height, bufferData, 2);
            }
            break;
    }
}

void GLES20Renderer::render(Matrix44F imageProjection)
{
    GLint bak_blend, bak_depth, bak_fbo, bak_tex, bak_arr_buf, bak_ele_arr_buf, bak_cull, bak_program, bak_active_tex, bak_tex_1, bak_tex_2;
    GLint bak_viewport[4];
    glGetIntegerv(GL_BLEND, &bak_blend);
    glGetIntegerv(GL_DEPTH_TEST, &bak_depth);
    glGetIntegerv(GL_CULL_FACE, &bak_cull);
    glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &bak_arr_buf);
    glGetIntegerv(GL_ELEMENT_ARRAY_BUFFER_BINDING, &bak_ele_arr_buf);
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &bak_fbo);
    glGetIntegerv(GL_VIEWPORT, &bak_viewport[0]);
    glGetIntegerv(GL_CURRENT_PROGRAM, &bak_program);
    glGetIntegerv(GL_ACTIVE_TEXTURE, &bak_active_tex);
    glActiveTexture(GL_TEXTURE0);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex);
    glActiveTexture(GL_TEXTURE1);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex_1);
    glActiveTexture(GL_TEXTURE2);
    glGetIntegerv(GL_TEXTURE_BINDING_2D, &bak_tex_2);

    int va[2] = {-1, -1};
    int bak_va_binding[2] = {0, 0};
    int bak_va_enable[2], bak_va_size[2], bak_va_stride[2], bak_va_type[2], bak_va_norm[2];
    void* bak_va_pointer[2];

    glDisable(GL_DEPTH_TEST);
    glDisable(GL_BLEND);
    glDisable(GL_CULL_FACE);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    va[0] = background_coord_location_;
    va[1] = background_texture_location_;
    for (int i = 0; i < 2; ++i)
    {
        if (va[i] == -1)
            continue;
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING, &bak_va_binding[i]);
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_ENABLED, &bak_va_enable[i]);
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_SIZE, &bak_va_size[i]);
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_STRIDE, &bak_va_stride[i]);
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_TYPE, &bak_va_type[i]);
        glGetVertexAttribiv(va[i], GL_VERTEX_ATTRIB_ARRAY_NORMALIZED, &bak_va_norm[i]);
        glGetVertexAttribPointerv(va[i], GL_VERTEX_ATTRIB_ARRAY_POINTER, &bak_va_pointer[i]);
    }

    AutoRelease gl_restorer([&]()
    {
        if (bak_blend) glEnable(GL_BLEND);
        if (bak_depth) glEnable(GL_DEPTH_TEST);
        if (bak_cull) glEnable(GL_CULL_FACE);

        for (int i = 0; i < 2; ++i)
        {
            if (!bak_va_binding[i])
                continue;
            glBindBuffer(GL_ARRAY_BUFFER, bak_va_binding[i]);
            if (bak_va_enable[i])
                glEnableVertexAttribArray(va[i]);
            else
                glDisableVertexAttribArray(va[i]);
            glVertexAttribPointer(va[i], bak_va_size[i], bak_va_type[i], (GLboolean)bak_va_norm[i], bak_va_stride[i], bak_va_pointer[i]);
        }

        glBindBuffer(GL_ARRAY_BUFFER, bak_arr_buf);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bak_ele_arr_buf);
        glBindFramebuffer(GL_FRAMEBUFFER, bak_fbo);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, bak_tex);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, bak_tex_1);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, bak_tex_2);
        glActiveTexture(bak_active_tex);
        glViewport(bak_viewport[0], bak_viewport[1], bak_viewport[2], bak_viewport[3]);
        glUseProgram(bak_program);
    });

    glUseProgram(background_program_);
    glBindBuffer(GL_ARRAY_BUFFER, background_coord_vbo_);

    glEnableVertexAttribArray(background_coord_location_);
    glVertexAttribPointer(background_coord_location_, 3, GL_FLOAT, GL_FALSE, 0, 0);

    GLfloat vertices[] = {
            -1.0f,  -1.0f,  0.f,
            1.0f,  -1.0f,  0.f,
            1.0f,   1.0f,  0.f,
            -1.0f, 1.0f,  0.f,
    };

    auto v0 = mul(imageProjection, Vec4F{{vertices[0], vertices[1], vertices[2], 1}});
    auto v1 = mul(imageProjection, Vec4F{{vertices[3], vertices[4], vertices[5], 1}});
    auto v2 = mul(imageProjection, Vec4F{{vertices[6], vertices[7], vertices[8], 1}});
    auto v3 = mul(imageProjection, Vec4F{{vertices[9], vertices[10], vertices[11], 1}});
    std::copy_n(v0.data.data(), 3, &vertices[0]);
    std::copy_n(v1.data.data(), 3, &vertices[3]);
    std::copy_n(v2.data.data(), 3, &vertices[6]);
    std::copy_n(v3.data.data(), 3, &vertices[9]);

    glBufferData(GL_ARRAY_BUFFER, sizeof(GLfloat) * 12, vertices, GL_DYNAMIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, background_texture_vbo_);
    glEnableVertexAttribArray(background_texture_location_);
    glVertexAttribPointer(background_texture_location_, 2, GL_FLOAT, GL_FALSE, 0, 0);

    switch (background_shader_)
    {
        case FrameShader::RGB:
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, background_texture_id_);
            break;
        case FrameShader::YUV:
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, background_texture_id_);
            if (current_format_ == PixelFormat::YUV_NV21 || current_format_ == PixelFormat::YUV_NV12)
            {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, background_texture_uv_id_);
            }
            else
            {
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, background_texture_u_id_);
                glActiveTexture(GL_TEXTURE2);
                glBindTexture(GL_TEXTURE_2D, background_texture_v_id_);
            }
            break;
    }

    glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
}

void GLES20Renderer::retrieveFrame(PixelFormat format, int width, int height, void* bufferData, int retrieve_count)
{
    if (retrieve_count == 0)
    {
        if (width & 0x3)
        {
            if (width & 0x1)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            else
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2);
        }

        switch (background_shader_)
        {
            case FrameShader::RGB:
            {
                switch (format)
                {
                    case PixelFormat::Unknown:
                        glBindTexture(GL_TEXTURE_2D, 0);
                        break;
                    case PixelFormat::Gray:
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufferData);
                        break;
                    case PixelFormat::BGR888:
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, bufferData);
                        break;
                    case PixelFormat::RGB888:
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, bufferData);
                        break;
                    case PixelFormat::RGBA8888:
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bufferData);
                        break;
                    case PixelFormat::BGRA8888:
                        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bufferData);
                        break;
                    default:
                        glBindTexture(GL_TEXTURE_2D, 0);
                }
            }
                break;
            case FrameShader::YUV:
                if (format == PixelFormat::YUV_NV21 || format == PixelFormat::YUV_NV12 ||
                    format == PixelFormat::YUV_I420 || format == PixelFormat::YUV_YV12)
                {
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, bufferData);
                }
                else
                {
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 4, 4, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, yuv_black);
                }
                break;
            default:
                break;
        }
    }
    else if (retrieve_count == 1 || retrieve_count == 2)
    {
        if (background_shader_ != FrameShader::YUV) { return; }
        if (width & 0x7)
        {
            if (width & 0x3)
                glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            else
                glPixelStorei(GL_UNPACK_ALIGNMENT, 2);
        }
        unsigned char* data = nullptr;
        GLint type = GL_LUMINANCE_ALPHA;
        if (format == PixelFormat::YUV_NV21 || format == PixelFormat::YUV_NV12)
        {
            data = (unsigned char *)bufferData + width * height;
        }
        else if (format == PixelFormat::YUV_I420)
        {
            type = GL_LUMINANCE;
            if (retrieve_count == 1) //U
            {
                data = (unsigned char *)bufferData + width * height;
            }
            else if (retrieve_count == 2) //V
            {
                data = (unsigned char *)bufferData + width * height * 5 / 4;
            }
        }
        else if (format == PixelFormat::YUV_YV12)
        {
            type = GL_LUMINANCE;
            if (retrieve_count == 1) //U
            {
                data = (unsigned char *)bufferData + width * height * 5 / 4;
            }
            else if (retrieve_count == 2) //V
            {
                data = (unsigned char *)bufferData + width * height;
            }
        }
        glTexImage2D(GL_TEXTURE_2D, 0, type, width / 2, height / 2, 0, type, GL_UNSIGNED_BYTE, data);
    }
}

bool GLES20Renderer::initialize(PixelFormat format)
{
    if (format == PixelFormat::Unknown)
    {
        return false;
    }
    background_program_ = glCreateProgram();
    GLuint vertShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertShader, 1, &videobackground_vert, NULL);
    glCompileShader(vertShader);
    GLuint fragShader = glCreateShader(GL_FRAGMENT_SHADER);

    switch (format)
    {
        case PixelFormat::Gray:
        case PixelFormat::RGB888:
        case PixelFormat::RGBA8888:
            background_shader_ = FrameShader::RGB;
            glShaderSource(fragShader, 1, &videobackground_rgb_frag, NULL);
            break;
        case PixelFormat::BGR888:
        case PixelFormat::BGRA8888:
            background_shader_ = FrameShader::RGB;
            glShaderSource(fragShader, 1, &videobackground_bgr_frag, NULL);
            break;
        case PixelFormat::YUV_NV21:
            background_shader_ = FrameShader::YUV;
            glShaderSource(fragShader, 1, &videobackground_yuv_nv21_frag, NULL);
            break;
        case PixelFormat::YUV_NV12:
            background_shader_ = FrameShader::YUV;
            glShaderSource(fragShader, 1, &videobackground_yuv_nv12_frag, NULL);
            break;
        case PixelFormat::YUV_I420:
        case PixelFormat::YUV_YV12:
            background_shader_ = FrameShader::YUV;
            glShaderSource(fragShader, 1, &videobackground_yuv_i420_yv12_frag, NULL);
            break;
        default:
            break;
    }
    glCompileShader(fragShader);
    glAttachShader(background_program_, vertShader);
    glAttachShader(background_program_, fragShader);

    GLint compileSuccess;
    glGetShaderiv(vertShader, GL_COMPILE_STATUS, &compileSuccess);
    if (compileSuccess == GL_FALSE)
    {
        GLchar messages[256];
        glGetShaderInfoLog(vertShader, sizeof(messages), 0, &messages[0]);
    }
    glGetShaderiv(fragShader, GL_COMPILE_STATUS, &compileSuccess);
    if (compileSuccess == GL_FALSE)
    {
        GLchar messages[256];
        glGetShaderInfoLog(fragShader, sizeof(messages), 0, &messages[0]);
    }
    glLinkProgram(background_program_);
    glDeleteShader(vertShader);
    glDeleteShader(fragShader);

    GLint linkstatus = 0;
    glGetProgramiv(background_program_, GL_LINK_STATUS, &linkstatus);
    glUseProgram(background_program_);
    background_coord_location_ = glGetAttribLocation(background_program_, "coord");
    background_texture_location_ = glGetAttribLocation(background_program_, "texCoord");

    glGenBuffers(1, &background_coord_vbo_);
    glBindBuffer(GL_ARRAY_BUFFER, background_coord_vbo_);
    const GLfloat coord[] = {-1.0f, -1.0f, 0.f, 1.0f, -1.0f, 0.f, 1.0f, 1.0f, 0.f, -1.0f, 1.0f, 0.f};
    glBufferData(GL_ARRAY_BUFFER, sizeof(coord), coord, GL_DYNAMIC_DRAW);
    glGenBuffers(1, &background_texture_vbo_);
    glBindBuffer(GL_ARRAY_BUFFER, background_texture_vbo_);
    static const GLfloat texcoord[] = {0.f, 1.f, 1.f, 1.f, 1.f, 0.f, 0.f, 0.f}; //input texture data is Y-inverted
    glBufferData(GL_ARRAY_BUFFER, 8 * sizeof(GLfloat), texcoord, GL_STATIC_DRAW);

    glUniform1i(glGetUniformLocation(background_program_, "texture"), 0);
    glGenTextures(1, &background_texture_id_);
    glBindTexture(GL_TEXTURE_2D, background_texture_id_);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    switch (background_shader_)
    {
        case FrameShader::RGB:
            break;
        case FrameShader::YUV:
            if (format == PixelFormat::YUV_NV21 || format == PixelFormat::YUV_NV12)
            {
                glUniform1i(glGetUniformLocation(background_program_, "uv_texture"), 1);
                glGenTextures(1, &background_texture_uv_id_);
                glBindTexture(GL_TEXTURE_2D, background_texture_uv_id_);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            }
            else
            {
                glUniform1i(glGetUniformLocation(background_program_, "u_texture"), 1);
                glGenTextures(1, &background_texture_u_id_);
                glBindTexture(GL_TEXTURE_2D, background_texture_u_id_);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

                glUniform1i(glGetUniformLocation(background_program_, "v_texture"), 2);
                glGenTextures(1, &background_texture_v_id_);
                glBindTexture(GL_TEXTURE_2D, background_texture_v_id_);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            }
            break;
        default:
            break;
    }
    glGenFramebuffers(1, &background_texture_fbo_);
    initialized_ = true;
    return true;
}

void GLES20Renderer::finalize(PixelFormat format)
{
    if (!initialized_) { return; }

    if (eglGetCurrentContext() == current_context_)
    {
        glGetError();

        glDeleteProgram(background_program_);
        glDeleteBuffers(1, &background_coord_vbo_);
        glDeleteBuffers(1, &background_texture_vbo_);
        glDeleteFramebuffers(1, &background_texture_fbo_);
        glDeleteTextures(1, &background_texture_id_);
        switch (background_shader_)
        {
            case FrameShader::RGB:
                break;
            case FrameShader::YUV:
                if (format == PixelFormat::YUV_NV21 || format == PixelFormat::YUV_NV12)
                {
                    glDeleteTextures(1, &background_texture_uv_id_);
                }
                else
                {
                    glDeleteTextures(1, &background_texture_u_id_);
                    glDeleteTextures(1, &background_texture_v_id_);
                }
                break;
            default:
                break;
        }
        initialized_ = false;

        glGetError();
    }
}

Renderer::Renderer()
{
    impl_ = std::make_shared<GLES20Renderer>();
}

Renderer::~Renderer()
{
}

void Renderer::upload(PixelFormat format, int width, int height, void* bufferData)
{
    impl_->upload(format, width, height, bufferData);
}

void Renderer::render(Matrix44F imageProjection)
{
    impl_->render(imageProjection);
}
