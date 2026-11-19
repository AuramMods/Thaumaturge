package com.aurammods.thaumaturge.testing;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

/** Exercises the same GLFW version as Minecraft 1.20.1, without loading the mod. */
public final class BackgroundWindowSmoke {
    private BackgroundWindowSmoke() {}

    public static void main(String[] arguments) {
        boolean hidden = arguments.length > 0 && "hidden".equals(arguments[0]);
        int expectedVisibility = hidden ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE;
        GLFWErrorCallback errors = GLFWErrorCallback.createPrint(System.err).set();
        long window = 0;
        try {
            if (!GLFW.glfwInit()) {
                throw new IllegalStateException("GLFW initialization failed");
            }
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, hidden ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
            // Exercise caller attempts to override focus; the agent must win.
            GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_TRUE);
            window = GLFW.glfwCreateWindow(320, 160, "Thaumaturge background window test", 0, 0);
            if (window == 0) {
                throw new IllegalStateException("GLFW window creation failed");
            }
            GLFW.glfwShowWindow(window);
            GLFW.glfwFocusWindow(window);
            long until = System.nanoTime() + 3_000_000_000L;
            int samples = 0;
            while (System.nanoTime() < until) {
                GLFW.glfwPollEvents();
                if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUSED) != GLFW.GLFW_FALSE) {
                    throw new AssertionError("Background test window acquired keyboard focus");
                }
                if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_FOCUS_ON_SHOW) != GLFW.GLFW_FALSE) {
                    throw new AssertionError("Background test window has focus-on-show enabled");
                }
                if (GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_VISIBLE) != expectedVisibility) {
                    throw new AssertionError("Background test window has the wrong visibility");
                }
                samples++;
                GLFW.glfwWaitEventsTimeout(0.05);
            }
            System.out.println("Background GLFW window stayed " + (hidden ? "hidden and " : "")
                    + "unfocused for " + samples + " samples");
        } finally {
            if (window != 0) {
                GLFW.glfwDestroyWindow(window);
            }
            GLFW.glfwTerminate();
            errors.free();
        }
    }
}
