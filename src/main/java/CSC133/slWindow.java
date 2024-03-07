package csc133;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;

import static csc133.spot.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class slWindow {

    private static long my_oglwindow = 0;
    public static int WIN_POS_X = 50, WIN_POS_Y = 150;
    public static String WINDOW_TITLE = "CSC 133";

    private static GLFWKeyCallback keyCallback;
    private static GLFWFramebufferSizeCallback fbCallback;

    private static void slWindow() {

    }  //  private static void slWindow()

    public static long get_oglwindow(int width, int height) {
        if (my_oglwindow == 0 || WIN_WIDTH != width || WIN_HEIGHT == height) {
            WIN_WIDTH = width;
            WIN_HEIGHT = height;

            GLFWErrorCallback errorCallback;
            if (my_oglwindow == NULL) {
                glfwSetErrorCallback(errorCallback =
                        GLFWErrorCallback.createPrint(System.err));
                if (!glfwInit()) {
                    throw new IllegalStateException("Unable to initialize GLFW");
                }
                glfwDefaultWindowHints();
                glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
                glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
                glfwWindowHint(GLFW_SAMPLES, 8);
            }
            my_oglwindow = glfwCreateWindow(WIN_WIDTH, WIN_HEIGHT, WINDOW_TITLE, NULL, NULL);
            glfwSetKeyCallback(my_oglwindow, keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int
                        mods) {
                    if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                        glfwSetWindowShouldClose(window, true);
                }
            });

            if (my_oglwindow == NULL) {
                throw new RuntimeException("Failed to create the GLFW window");
            }

            glfwSetFramebufferSizeCallback(my_oglwindow, fbCallback = new
                    GLFWFramebufferSizeCallback() {
                        @Override
                        public void invoke(long window, int w, int h) {
                            if (w > 0 && h > 0) {
                                WIN_WIDTH = w;
                                WIN_HEIGHT = h;
                            } }
                    });
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(my_oglwindow, WIN_POS_X, WIN_POS_Y);
            glfwMakeContextCurrent(my_oglwindow);
            int VSYNC_INTERVAL = 1;
            glfwSwapInterval(VSYNC_INTERVAL);
            glfwShowWindow(my_oglwindow);
        }
        return my_oglwindow;
    }  // public static slWindow get_oglwindow(int width, int height)

    public static void destroy_oglwindow() {
        glfwDestroyWindow(my_oglwindow);
        keyCallback.free();
        fbCallback.free();

    }  //  public static void destroy_oglwindow()

    public static long get_oglwindow() {
        return get_oglwindow(WIN_WIDTH, WIN_HEIGHT);
    }  //  public static slWindow get_oglwindow

}  // public class slWindow
