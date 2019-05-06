import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClear;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL15.glDrawArrays;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
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
        // Clear the screen to black
        glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw a triangle from the 3 vertices
        glDrawArrays(GL_TRIANGLES, 0, 3);

        glfwSwapBuffers(window);

    }

    private String createFragmentShader() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/java/screen.frag")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create fragmentShader");
        }
        return null;
    }

    private String createVertexShader() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/java/screen.vert")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create vertexShader");
        }
        return null;
    }

    private void init() {

        if (!glfwInit()) {
            return;
        }
        initializeWindow();

        //Creating a VertexArrayObject
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // Create a Vertex Buffer Object and copy the vertex data to it
        int vbo = GL15.glGenBuffers();
        float[] vertices = {
                0.0f, 0.5f, // Vertex 1 (X, Y)
                0.5f, -0.5f, // Vertex 2 (X, Y)
                -0.5f, -0.5f  // Vertex 3 (X, Y)
        };
        FloatBuffer buffer = FloatBuffer.wrap(vertices);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        //Create Shaders:
        String fragmentSource = createFragmentShader();
        String vertexSource = createVertexShader();
        //Compile vertexShader:
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile vertexShader:\n" + glGetShaderInfoLog(vertexShader, 512));
        }
        //Compile fragmentShader:
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile fragmentShader: \n" + glGetShaderInfoLog(fragmentShader, 512));
        }
        // Link the vertex and fragment shader into a shader program
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindFragDataLocation(shaderProgram, 0, "outColor");
        glLinkProgram(shaderProgram);
        glUseProgram(shaderProgram);

        // Specify the layout of the vertex data
        int posAttrib = glGetAttribLocation(shaderProgram, "position");
        //how the data for that input is retrieved from the array:
        glEnableVertexAttribArray(posAttrib);
        glVertexAttribPointer(posAttrib, 2, GL_FLOAT, false, 0, 0);

    }

    private String readfile(String filename) {
        StringBuilder string = new StringBuilder();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(new File("./shaders/" + filename)));
            String line;
            while ((line = br.readLine()) != null) {
                string.append(line);
                string.append("\n");
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string.toString();
    }

    private void initializeWindow() {
        int w = 1024;
        int h = 768;
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(w, h, "OPENGL", NULL, NULL);
        if (window == NULL) {
            return;
        }
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, vidmode.width() / 2 - w / 2, vidmode.height() / 2 - h / 2);
        glfwMakeContextCurrent(window);
        createCapabilities();
        glfwShowWindow(window);

        //clear the window to be black:
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

    }

    public static void main(String[] args) {
        new Main();
    }

}