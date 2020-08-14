//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

#include "boxrenderer.hpp"

#include <GLES2/gl2.h>
#include <EGL/egl.h>

using namespace easyar;

const char* box_vert="uniform mat4 trans;\n"
        "uniform mat4 proj;\n"
        "attribute vec4 coord;\n"
        "attribute vec4 color;\n"
        "varying vec4 vcolor;\n"
        "\n"
        "void main(void)\n"
        "{\n"
        "    vcolor = color;\n"
        "    gl_Position = proj*trans*coord;\n"
        "}\n"
        "\n"
;

const char* box_frag="#ifdef GL_ES\n"
        "precision highp float;\n"
        "#endif\n"
        "varying vec4 vcolor;\n"
        "\n"
        "void main(void)\n"
        "{\n"
        "    gl_FragColor = vcolor;\n"
        "}\n"
        "\n"
;

static void getGLMatrix(const Matrix44F &m, float (*a)[16])
{
    const float (&d)[16] = m.data;
    (*a)[0] = d[0];
    (*a)[1] = d[4];
    (*a)[2] = d[8];
    (*a)[3] = d[12];
    (*a)[4] = d[1];
    (*a)[5] = d[5];
    (*a)[6] = d[9];
    (*a)[7] = d[13];
    (*a)[8] = d[2];
    (*a)[9] = d[6];
    (*a)[10] = d[10];
    (*a)[11] = d[14];
    (*a)[12] = d[3];
    (*a)[13] = d[7];
    (*a)[14] = d[11];
    (*a)[15] = d[15];
}

BoxRenderer::BoxRenderer()
{
    current_context = eglGetCurrentContext();
    program_box = glCreateProgram();
    GLuint vertShader = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertShader, 1, &box_vert, 0);
    glCompileShader(vertShader);
    GLuint fragShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragShader, 1, &box_frag, 0);
    glCompileShader(fragShader);
    glAttachShader(program_box, vertShader);
    glAttachShader(program_box, fragShader);
    glLinkProgram(program_box);
    glUseProgram(program_box);
    pos_coord_box = glGetAttribLocation(program_box, "coord");
    pos_color_box = glGetAttribLocation(program_box, "color");
    pos_trans_box = glGetUniformLocation(program_box, "trans");
    pos_proj_box = glGetUniformLocation(program_box, "proj");

    glGenBuffers(1, &vbo_coord_box);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_coord_box);
    const GLfloat cube_vertices[8][3] = {
        /* +z */{1.0f / 2, 1.0f / 2, 0.01f / 2}, {1.0f / 2, -1.0f / 2, 0.01f / 2}, {-1.0f / 2, -1.0f / 2, 0.01f / 2}, {-1.0f / 2, 1.0f / 2, 0.01f / 2},
        /* -z */{1.0f / 2, 1.0f / 2, -0.01f / 2}, {1.0f / 2, -1.0f / 2, -0.01f / 2}, {-1.0f / 2, -1.0f / 2, -0.01f / 2}, {-1.0f / 2, 1.0f / 2, -0.01f / 2}};
    glBufferData(GL_ARRAY_BUFFER, sizeof(cube_vertices), cube_vertices, GL_DYNAMIC_DRAW);

    glGenBuffers(1, &vbo_color_box);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_color_box);
    const GLubyte cube_vertex_colors[8][4] = {
        {255, 0, 0, 128}, {0, 255, 0, 128}, {0, 0, 255, 128}, {0, 0, 0, 128},
        {0, 255, 255, 128}, {255, 0, 255, 128}, {255, 255, 0, 128}, {255, 255, 255, 128}};
    glBufferData(GL_ARRAY_BUFFER, sizeof(cube_vertex_colors), cube_vertex_colors, GL_STATIC_DRAW);

    glGenBuffers(1, &vbo_color_box_2);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_color_box_2);
    const GLubyte cube_vertex_colors_2[8][4] = {
        {255, 0, 0, 255}, {255, 255, 0, 255}, {0, 255, 0, 255}, {255, 0, 255, 255},
        {255, 0, 255, 255}, {255, 255, 255, 255}, {0, 255, 255, 255}, {255, 0, 255, 255}};
    glBufferData(GL_ARRAY_BUFFER, sizeof(cube_vertex_colors_2), cube_vertex_colors_2, GL_STATIC_DRAW);

    glGenBuffers(1, &vbo_faces_box);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box);
    const GLushort cube_faces[6][4] = {
        /* +x */ {0, 1, 5, 4},
        /* -x */ {3, 7, 6, 2},
        /* +y */ {0, 4, 7, 3},
        /* -y */ {1, 2, 6, 5},
        /* +z */ {0, 3, 2, 1},
        /* -z */ {4, 5, 6, 7}
    };
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(cube_faces), cube_faces, GL_STATIC_DRAW);
}

BoxRenderer::~BoxRenderer()
{
    if (eglGetCurrentContext() == current_context) {
        glDeleteProgram(program_box);

        glDeleteBuffers(1, &vbo_coord_box);
        glDeleteBuffers(1, &vbo_color_box);
        glDeleteBuffers(1, &vbo_color_box_2);
        glDeleteTextures(1, &vbo_faces_box);
    }
}

void BoxRenderer::render(const Matrix44F& projectionMatrix, const Matrix44F& cameraview, Vec2F size)
{
    float size0 = size.data[0];
    float size1 = size.data[1];

    glBindBuffer(GL_ARRAY_BUFFER, vbo_coord_box);
    float height = size0 / 1000;
    const GLfloat cube_vertices[8][3] = {
        /* +z */{size0 / 2, size1 / 2, height / 2}, {size0 / 2, -size1 / 2, height / 2}, {-size0 / 2, -size1 / 2, height / 2}, {-size0 / 2, size1 / 2, height / 2},
        /* -z */{size0 / 2, size1 / 2, 0}, {size0 / 2, -size1 / 2, 0}, {-size0 / 2, -size1 / 2, 0}, {-size0 / 2, size1 / 2, 0}};
    glBufferData(GL_ARRAY_BUFFER, sizeof(cube_vertices), cube_vertices, GL_DYNAMIC_DRAW);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_DEPTH_TEST);
    glUseProgram(program_box);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_coord_box);
    glEnableVertexAttribArray(pos_coord_box);
    glVertexAttribPointer(pos_coord_box, 3, GL_FLOAT, GL_FALSE, 0, 0);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_color_box);
    glEnableVertexAttribArray(pos_color_box);
    glVertexAttribPointer(pos_color_box, 4, GL_UNSIGNED_BYTE, GL_TRUE, 0, 0);
    float cameraview_data[16] = {};
    float projectionMatrix_data[16] = {};
    getGLMatrix(cameraview, &cameraview_data);
    getGLMatrix(projectionMatrix, &projectionMatrix_data);
    glUniformMatrix4fv(pos_trans_box, 1, 0, cameraview_data);
    glUniformMatrix4fv(pos_proj_box, 1, 0, projectionMatrix_data);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box);
    for(int i = 0; i < 6; i++) {
        glDrawElements(GL_TRIANGLE_FAN, 4, GL_UNSIGNED_SHORT, (void*)(i * 4 * sizeof(GLushort)));
    }

    glBindBuffer(GL_ARRAY_BUFFER, vbo_coord_box);
    const GLfloat cube_vertices_2[8][3] = {
        /* +z */{size0 / 4, size1 / 4, size0 / 4},{size0 / 4, -size1 / 4, size0 / 4},{-size0 / 4, -size1 / 4, size0 / 4},{-size0 / 4, size1 / 4, size0 / 4},
        /* -z */{size0 / 4, size1 / 4, 0},{size0 / 4, -size1 / 4, 0},{-size0 / 4, -size1 / 4, 0},{-size0 / 4, size1 / 4, 0}};
    glBufferData(GL_ARRAY_BUFFER, sizeof(cube_vertices_2), cube_vertices_2, GL_DYNAMIC_DRAW);
    glEnableVertexAttribArray(pos_coord_box);
    glVertexAttribPointer(pos_coord_box, 3, GL_FLOAT, GL_FALSE, 0, 0);
    glBindBuffer(GL_ARRAY_BUFFER, vbo_color_box_2);
    glEnableVertexAttribArray(pos_color_box);
    glVertexAttribPointer(pos_color_box, 4, GL_UNSIGNED_BYTE, GL_TRUE, 0, 0);
    for(int i = 0; i < 6; i++) {
        glDrawElements(GL_TRIANGLE_FAN, 4, GL_UNSIGNED_SHORT, (void*)(i * 4 * sizeof(GLushort)));
    }
}
