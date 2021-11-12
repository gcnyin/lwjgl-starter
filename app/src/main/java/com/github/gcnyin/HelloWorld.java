package com.github.gcnyin;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class HelloWorld {

    GLFWKeyCallback keyCallback;
    GLFWGamepadState state;

    public void run() {
        if (!glfwInit()) {
            System.out.println("glfwInit has wrong");
            System.exit(-1);
        }

        long window = glfwCreateWindow(800, 600, Version.getVersion(), MemoryUtil.NULL, MemoryUtil.NULL);

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

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        keyCallback.free();
        glfwTerminate();
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }

}