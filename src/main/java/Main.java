import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private boolean running = true; //game runs until running is set to false
    private Box box;
    private long window;

    public Main() {
        running = true;
        init();
        while (running) {
            update();
            render();
            if (glfwWindowShouldClose(window)) {
                running = false;
            }
        }

    }

    private void update() {
        glfwPollEvents();
    }

    private void render() {

        float vertices[] = {
                0.0f,  0.5f, // Vertex 1 (X, Y)
                0.5f, -0.5f, // Vertex 2 (X, Y)
                -0.5f, -0.5f  // Vertex 3 (X, Y)
        };
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices.length);
        vertexData.put(vertices);
        vertexData.flip();

        int vbo = GL15.glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER,(vbo));
        glBufferData(GL_ARRAY_BUFFER,vertexData,GL_STATIC_DRAW);

    }


    public static int createVBOID() {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        glGenBuffers(buffer);
        return buffer.get(0);
    }

    private void init() {
        int w = 1024;
        int h = 768;
        if (!glfwInit()) {
            return;
        }
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(w, h, "test", NULL, NULL);
        if (window == NULL) {
            return;
        }
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, vidmode.width()/2-w/2, vidmode.height()/2-h/2);
        glfwMakeContextCurrent(window);
        createCapabilities();
        glfwShowWindow(window);
    }

    public static void main(String[] args) {
        new Main();
    }

}