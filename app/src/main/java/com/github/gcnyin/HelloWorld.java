package com.github.gcnyin;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Scanner;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class HelloWorld {
    public static void main(String[] args) {
        try {
            new HelloWorld().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String loadResource(String filename) throws IOException {
        String result;
        try (InputStream in = HelloWorld.class.getResourceAsStream(filename);
             Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    private static int createShader(int programId, String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 2048));
        }
        glAttachShader(programId, shaderId);
        return shaderId;
    }

    GLFWKeyCallback keyCallback;

    public void run() throws Exception {
        if (!glfwInit()) {
            System.out.println("glfwInit has wrong");
            System.exit(-1);
        }

        glfwDefaultWindowHints();
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        int WIDTH = 800;
        int HEIGHT = 600;

        long window = glfwCreateWindow(WIDTH, HEIGHT, "LWJGL " + Version.getVersion(), MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            glfwTerminate();
            System.exit(-1);
        }

        glfwMakeContextCurrent(window);
        createCapabilities();

        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        });

        glClearColor(1.0f, 0.5f, 0.0f, 0.0f);

        final int programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        final int vertexShaderId = createShader(programId, loadResource("/vertex.vs"), GL_VERTEX_SHADER);
        final int fragmentShaderId = createShader(programId, loadResource("/fragment.fs"), GL_FRAGMENT_SHADER);
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        float[] vertices = new float[]{
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };

        int vboId, vaoId;

        FloatBuffer verticesBuffer = null;
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();

            // Create the VAO and bind to it
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Create the VBO and bint to it
            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            // Enable location 0
            glEnableVertexAttribArray(0);
            // Define structure of the data
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Unbind the VBO
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Unbind the VAO
            glBindVertexArray(0);
        } finally {
            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
            }
        }

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(programId);
            // Bind to the VAO
            glBindVertexArray(vaoId);
            // Draw the vertices
            glDrawArrays(GL_TRIANGLES, 0, 3);
            // Restore state
            glBindVertexArray(0);
            glUseProgram(0);
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        keyCallback.free();
        glfwTerminate();
    }
}