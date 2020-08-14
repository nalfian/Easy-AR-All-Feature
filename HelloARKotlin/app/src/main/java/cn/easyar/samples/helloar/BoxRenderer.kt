//================================================================================================================================
//
// Copyright (c) 2015-2020 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar

import android.opengl.GLES20

import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext

import cn.easyar.Vec2F
import cn.easyar.Matrix44F

// all methods of this class can only be called on one thread with the same OpenGLES context
class BoxRenderer {
    private var current_context: EGLContext? = null
    private val program_box: Int
    private val pos_coord_box: Int
    private val pos_color_box: Int
    private val pos_trans_box: Int
    private val pos_proj_box: Int
    private val vbo_coord_box: Int
    private val vbo_color_box: Int
    private val vbo_color_box_2: Int
    private val vbo_faces_box: Int

    private val box_vert = ("uniform mat4 trans;\n"
            + "uniform mat4 proj;\n"
            + "attribute vec4 coord;\n"
            + "attribute vec4 color;\n"
            + "varying vec4 vcolor;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    vcolor = color;\n"
            + "    gl_Position = proj*trans*coord;\n"
            + "}\n"
            + "\n")

    private val box_frag = ("#ifdef GL_ES\n"
            + "precision highp float;\n"
            + "#endif\n"
            + "varying vec4 vcolor;\n"
            + "\n"
            + "void main(void)\n"
            + "{\n"
            + "    gl_FragColor = vcolor;\n"
            + "}\n"
            + "\n")

    private fun flatten(a: Array<FloatArray>): FloatArray {
        var size = 0
        run {
            var k = 0
            while (k < a.size) {
                size += a[k].size
                k += 1
            }
        }
        val l = FloatArray(size)
        var offset = 0
        var k = 0
        while (k < a.size) {
            System.arraycopy(a[k], 0, l, offset, a[k].size)
            offset += a[k].size
            k += 1
        }
        return l
    }

    private fun flatten(a: Array<IntArray>): IntArray {
        var size = 0
        run {
            var k = 0
            while (k < a.size) {
                size += a[k].size
                k += 1
            }
        }
        val l = IntArray(size)
        var offset = 0
        var k = 0
        while (k < a.size) {
            System.arraycopy(a[k], 0, l, offset, a[k].size)
            offset += a[k].size
            k += 1
        }
        return l
    }

    private fun flatten(a: Array<ShortArray>): ShortArray {
        var size = 0
        run {
            var k = 0
            while (k < a.size) {
                size += a[k].size
                k += 1
            }
        }
        val l = ShortArray(size)
        var offset = 0
        var k = 0
        while (k < a.size) {
            System.arraycopy(a[k], 0, l, offset, a[k].size)
            offset += a[k].size
            k += 1
        }
        return l
    }

    private fun flatten(a: Array<ByteArray>): ByteArray {
        var size = 0
        run {
            var k = 0
            while (k < a.size) {
                size += a[k].size
                k += 1
            }
        }
        val l = ByteArray(size)
        var offset = 0
        var k = 0
        while (k < a.size) {
            System.arraycopy(a[k], 0, l, offset, a[k].size)
            offset += a[k].size
            k += 1
        }
        return l
    }

    private fun byteArrayFromIntArray(a: IntArray): ByteArray {
        val l = ByteArray(a.size)
        var k = 0
        while (k < a.size) {
            l[k] = (a[k] and 0xFF).toByte()
            k += 1
        }
        return l
    }

    private fun generateOneBuffer(): Int {
        val buffer = intArrayOf(0)
        GLES20.glGenBuffers(1, buffer, 0)
        return buffer[0]
    }

    private fun deleteOneBuffer(id: Int) {
        val buffer = intArrayOf(id)
        GLES20.glDeleteBuffers(1, buffer, 0)
    }

    private fun getGLMatrix(m: Matrix44F): FloatArray {
        val d = m.data
        return floatArrayOf(d[0], d[4], d[8], d[12], d[1], d[5], d[9], d[13], d[2], d[6], d[10], d[14], d[3], d[7], d[11], d[15])
    }

    init {
        current_context = (EGLContext.getEGL() as EGL10).eglGetCurrentContext()
        program_box = GLES20.glCreateProgram()
        val vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertShader, box_vert)
        GLES20.glCompileShader(vertShader)
        val fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragShader, box_frag)
        GLES20.glCompileShader(fragShader)
        GLES20.glAttachShader(program_box, vertShader)
        GLES20.glAttachShader(program_box, fragShader)
        GLES20.glLinkProgram(program_box)
        GLES20.glUseProgram(program_box)
        GLES20.glDeleteShader(vertShader)
        GLES20.glDeleteShader(fragShader)
        pos_coord_box = GLES20.glGetAttribLocation(program_box, "coord")
        pos_color_box = GLES20.glGetAttribLocation(program_box, "color")
        pos_trans_box = GLES20.glGetUniformLocation(program_box, "trans")
        pos_proj_box = GLES20.glGetUniformLocation(program_box, "proj")

        vbo_coord_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val cube_vertices = arrayOf(
                /* +z */floatArrayOf(1.0f / 2, 1.0f / 2, 0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, 0.01f / 2),
                /* -z */floatArrayOf(1.0f / 2, 1.0f / 2, -0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, -0.01f / 2))
        val cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices))
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW)

        vbo_color_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box)
        val cube_vertex_colors = arrayOf(intArrayOf(255, 0, 0, 128), intArrayOf(0, 255, 0, 128), intArrayOf(0, 0, 255, 128), intArrayOf(0, 0, 0, 128), intArrayOf(0, 255, 255, 128), intArrayOf(255, 0, 255, 128), intArrayOf(255, 255, 0, 128), intArrayOf(255, 255, 255, 128))
        val cube_vertex_colors_buffer = ByteBuffer.wrap(byteArrayFromIntArray(flatten(cube_vertex_colors)))
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_buffer.limit(), cube_vertex_colors_buffer, GLES20.GL_STATIC_DRAW)

        vbo_color_box_2 = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box_2)
        val cube_vertex_colors_2 = arrayOf(intArrayOf(255, 0, 0, 255), intArrayOf(255, 255, 0, 255), intArrayOf(0, 255, 0, 255), intArrayOf(255, 0, 255, 255), intArrayOf(255, 0, 255, 255), intArrayOf(255, 255, 255, 255), intArrayOf(0, 255, 255, 255), intArrayOf(255, 0, 255, 255))
        val cube_vertex_colors_2_buffer = ByteBuffer.wrap(byteArrayFromIntArray(flatten(cube_vertex_colors_2)))
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_2_buffer.limit(), cube_vertex_colors_2_buffer, GLES20.GL_STATIC_DRAW)

        vbo_faces_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
        val cube_faces = arrayOf(
                /* +x */ shortArrayOf(0, 1, 5, 4),
                /* -x */ shortArrayOf(3, 7, 6, 2),
                /* +y */ shortArrayOf(0, 4, 7, 3),
                /* -y */ shortArrayOf(1, 2, 6, 5),
                /* +z */ shortArrayOf(0, 3, 2, 1),
                /* -z */ shortArrayOf(4, 5, 6, 7))
        val cube_faces_buffer = ShortBuffer.wrap(flatten(cube_faces))
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cube_faces_buffer.limit() * 2, cube_faces_buffer, GLES20.GL_STATIC_DRAW)
    }

    fun dispose() {
        if ((EGLContext.getEGL() as EGL10).eglGetCurrentContext() == current_context) { //destroy resources unless the context has lost
            GLES20.glDeleteProgram(program_box)

            deleteOneBuffer(vbo_coord_box)
            deleteOneBuffer(vbo_color_box)
            deleteOneBuffer(vbo_color_box_2)
            deleteOneBuffer(vbo_faces_box)
        }
    }

    fun render(projectionMatrix: Matrix44F, cameraview: Matrix44F, size: Vec2F) {
        val size0 = size.data[0]
        val size1 = size.data[1]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val height = size0 / 1000
        val cube_vertices = arrayOf(
                /* +z */floatArrayOf(size0 / 2, size1 / 2, height / 2), floatArrayOf(size0 / 2, -size1 / 2, height / 2), floatArrayOf(-size0 / 2, -size1 / 2, height / 2), floatArrayOf(-size0 / 2, size1 / 2, height / 2),
                /* -z */floatArrayOf(size0 / 2, size1 / 2, 0f), floatArrayOf(size0 / 2, -size1 / 2, 0f), floatArrayOf(-size0 / 2, -size1 / 2, 0f), floatArrayOf(-size0 / 2, size1 / 2, 0f))
        val cube_vertices_buffer = FloatBuffer.wrap(flatten(cube_vertices))
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glUseProgram(program_box)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        GLES20.glEnableVertexAttribArray(pos_coord_box)
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box)
        GLES20.glEnableVertexAttribArray(pos_color_box)
        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0)
        GLES20.glUniformMatrix4fv(pos_trans_box, 1, false, getGLMatrix(cameraview), 0)
        GLES20.glUniformMatrix4fv(pos_proj_box, 1, false, getGLMatrix(projectionMatrix), 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
        for (i in 0..5) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, i * 4 * 2)
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val cube_vertices_2 = arrayOf(
                /* +z */floatArrayOf(size0 / 4, size1 / 4, size0 / 4), floatArrayOf(size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, size1 / 4, size0 / 4),
                /* -z */floatArrayOf(size0 / 4, size1 / 4, 0f), floatArrayOf(size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, size1 / 4, 0f))
        val cube_vertices_2_buffer = FloatBuffer.wrap(flatten(cube_vertices_2))
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_2_buffer.limit() * 4, cube_vertices_2_buffer, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glEnableVertexAttribArray(pos_coord_box)
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box_2)
        GLES20.glEnableVertexAttribArray(pos_color_box)
        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0)
        for (i in 0..5) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, i * 4 * 2)
        }
    }
}
