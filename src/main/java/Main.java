import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDisable;
import static org.lwjgl.opengl.GL20.glDrawArrays;
import static org.lwjgl.opengl.GL20.glEnable;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static org.lwjgl.opengl.GL30.glBeginTransformFeedback;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glEndTransformFeedback;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL40.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL40.GL_INTERLEAVED_ATTRIBS;
import static org.lwjgl.opengl.GL40.GL_STATIC_READ;
import static org.lwjgl.opengl.GL40.glBindVertexArray;
import static org.lwjgl.opengl.GL40.glBufferSubData;
import static org.lwjgl.opengl.GL40.glClear;
import static org.lwjgl.opengl.GL40.glFlush;
import static org.lwjgl.opengl.GL40.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.glTransformFeedbackVaryings;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    //<editor-fold desc="Attributes">
    private long window;
    private int uniColor;
    private float pulseColor = 1;
    private boolean pulseUp = true;
    private int[] elements;
    private int renderProgram;
    private int geoProgram;
    private Matrix4f tree;
    private Matrix4f proj;
    private Matrix4f view;
    private int test = 0;
    private boolean testUp = true;
    private boolean rotateObject = true;
    private int triangles = 3;
    private int sizeOfFloat = 4;
    //    private float[] model;
    private int vertexArr;
    private int myBufferTriangle;
    private int renderVertexShader;
    private int geoVertexShader;
    private int geoGeometyShader;
    private int renderFragmentShader;
    private int pos;
    private int normal;
    private int colorAttrib;
    private int texAttrib;
    private int length;
    private FloatBuffer inputData;
    private int inputVBO;
    private int outputVBO;
    private int feedbackObject;
    private int queryObject;
    private CharSequence[] varyings;
    private float[] vert2;
    private int numberOfIterations = 10;
    private int numberOfVertices = 0;
    private int numberOfTriangles = 10;
    private int mybufferFeedback;
    private int renderVertex;
    private int renderPos;
    private int renderLength;
    private int renderNormal;
    private int currentVertexArray;
    private int currentFeedbackBuffer;
    private int lastVertexArray;
    private int lastFeedbackBuffer;
    private int swapVertexArray;
    private int swapFeedbackBuffer;
    private float rotationY;
    private float rotationX;
    private int windowWidth;
    private int windowHeight;
    private float aspect;
    private Vector3f camaraPos;
    private int treei;
    //</editor-fold>

    public Main() {
        init();
    }


    //<editor-fold desc="init">
    private void init() {
        initializeWindow();
        createAndCompileShaders();
        createProgrammAndLinkShaders();
        createModel();
        calculateNumberOfVertices(numberOfIterations);
        createArrayBuffer();
        createVertexArrayObject();
        createVertexAttribAndPointers();
        createTransformFeedbackBuffer();
        createSecondVertexArrayObject();
        createSecondVertexAttribAndPointers();
        setArrayAndBufferPointer();
        iterationsLoop();
        setCamera();
        calculateAspect();
        gameLoop();
        terminateApplication();
    }

    private void gameLoop() {
        while (!glfwWindowShouldClose(window)) {
            clearDisplay();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            double previous_seconds = glfwGetTime();
            double current_seconds = glfwGetTime();
            double elapsed_seconds = current_seconds - previous_seconds;
            previous_seconds = current_seconds;

            glBindVertexArray(renderVertex);
            glUseProgram(renderProgram);

            glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
            glfwSwapBuffers(window);
            glfwPollEvents();

            if (GLFW_PRESS == glfwGetKey(window, GLFW_KEY_ESCAPE))
                glfwSetWindowShouldClose(window, true);
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS)
                camaraPos.z += -10.0f * elapsed_seconds;
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS)
                camaraPos.z += 10.0f * elapsed_seconds;
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS)
                rotationX += 10.f * elapsed_seconds;
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS)
                rotationX -= 10.f * elapsed_seconds;
            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS)
                rotationY = (float) (+1.f * elapsed_seconds);
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS)
                rotationY = (float) (-1.f * elapsed_seconds);

            updateMatrices();
            calculateModel();
            calculateView();
            calculateProjection();

        }

    }

    private void terminateApplication() {
        System.out.println("YO: BYE!");
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        if (glfwSetErrorCallback(null) != null) {
            glfwSetErrorCallback(null).free();
        }
        System.exit(1);
    }


    private void updateMatrices() {
        tree = new Matrix4f().identity();
        tree.rotate((float) Math.toRadians(30), 1f, 0f, 0);

        view = new Matrix4f().identity();
        view.lookAt(
                new Vector3f(0.0f, 0.0f, 7f),       //eye
                new Vector3f(0, 0, 0),            //center
                new Vector3f(0.0f, 2f, 0f)    //up
        );

        proj = new Matrix4f().perspective(1, 1, 3, -3);
    }

    private void calculateAspect() {
        aspect = (float) windowWidth / (float) windowHeight;
    }

    private void setCamera() {
        camaraPos = new Vector3f(0.0f, 0.0f, 50.0f);
        rotationX = 0.0f;
        rotationY = 0.0f;
    }

    private void iterationsLoop() {
        for (int i = 0; i < numberOfIterations; ++i) {
            glBindVertexArray(currentVertexArray);
            glUseProgram(geoProgram);
            glBindBuffer(GL_ARRAY_BUFFER, currentFeedbackBuffer);
            glEnable(GL_RASTERIZER_DISCARD);
            glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, currentFeedbackBuffer);
            glBeginTransformFeedback(GL_TRIANGLES);
            glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
            glEndTransformFeedback();
            glDisable(GL_RASTERIZER_DISCARD);
            glFlush();

            swapVertexArrayAndBuffers();
        }
    }

    private void swapVertexArrayAndBuffers() {
        swapVertexArray = glGenVertexArrays();
        swapVertexArray = currentVertexArray;
        currentVertexArray = lastVertexArray;
        lastVertexArray = swapVertexArray;
//        System.out.println("swapVertexArray: " + swapVertexArray + "\n"
//            + "currentVertexArray: " + currentVertexArray + "\n"
//                + "lastVertexArray: " + lastVertexArray + "\n");

        swapFeedbackBuffer = glGenBuffers();
        swapFeedbackBuffer = currentFeedbackBuffer;
        currentFeedbackBuffer = lastFeedbackBuffer;
        lastFeedbackBuffer = swapFeedbackBuffer;
//        System.out.println("swapFeedbackBuffer: " + swapFeedbackBuffer + "\n"
//                + "currentFeedbackBuffer: " + currentFeedbackBuffer + "\n"
//                + "lastFeedbackBuffer: " + lastVertexArray + "\n");

    }

    private void setArrayAndBufferPointer() {
        currentVertexArray = glGenVertexArrays();
        currentVertexArray = vertexArr;
        currentFeedbackBuffer = glGenBuffers();
        currentVertexArray = mybufferFeedback;

        lastVertexArray = glGenVertexArrays();
        lastVertexArray = renderVertex;
        lastFeedbackBuffer = glGenBuffers();
        lastFeedbackBuffer = myBufferTriangle;


    }

    private void createSecondVertexAttribAndPointers() {

        renderPos = glGetAttribLocation(renderProgram, "position");
        glEnableVertexAttribArray(renderPos);
        glVertexAttribPointer(renderPos, 3, GL_FLOAT, false, 7 * sizeOfFloat, 0 * sizeOfFloat);

        renderLength = glGetAttribLocation(renderProgram, "length");
        glEnableVertexAttribArray(renderLength);
        glVertexAttribPointer(renderLength, 1, GL_FLOAT, false, 7 * sizeOfFloat, 3 * sizeOfFloat);

        renderNormal = glGetAttribLocation(renderProgram, "normal");
        glEnableVertexAttribArray(renderNormal);
        glVertexAttribPointer(renderNormal, 3, GL_FLOAT, false, 7 * sizeOfFloat, 4 * sizeOfFloat);

    }

    private void createSecondVertexArrayObject() {
        renderVertex = glGenVertexArrays();
        glBindVertexArray(renderVertex);
        glBindBuffer(GL_ARRAY_BUFFER, mybufferFeedback);

    }

    private void createTransformFeedbackBuffer() {
        mybufferFeedback = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mybufferFeedback);
        glBufferData(GL_ARRAY_BUFFER, vert2.length * numberOfVertices, GL_STATIC_READ);
    }

    private void createVertexArrayObject() {
        vertexArr = glGenVertexArrays();
        glBindVertexArray(vertexArr);
        glBindBuffer(GL_ARRAY_BUFFER, myBufferTriangle);
    }

    private void createArrayBuffer() {
        myBufferTriangle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, myBufferTriangle);
        glBufferData(GL_ARRAY_BUFFER, vert2.length * numberOfVertices, GL_STATIC_DRAW);

        FloatBuffer verticeBuffer = BufferUtils.createFloatBuffer(vert2.length * 500);
        verticeBuffer.put(vert2).flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0, verticeBuffer);
    }

    private void calculateNumberOfVertices(int iterations) {
        numberOfTriangles = (int) (4 * Math.pow(3, iterations) - 3);
        numberOfVertices = numberOfTriangles * 3;
    }


    private void initializeWindow() {

        if (!glfwInit()) {
            return;
        }

        windowWidth = 1024;
        windowHeight = 768;
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(windowWidth, windowHeight, "Rekursiver Pythagoras Baum - Real-Time Rendering", NULL, NULL);
        if (window == NULL) {
            return;
        }
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode != null ? vidmode.width() : 0) / 2 - windowWidth / 2, (vidmode != null ? vidmode.height() : 0) / 2 - windowHeight / 2);
        glfwMakeContextCurrent(window);

//        String renderer = glGetString(GL_RENDERER); /* get renderer string */
//        String version = glGetString(GL_VERSION); /* version as a string */
//        System.out.println("RENDERER: " + renderer + " // VERSION: " + version);

        createCapabilities();
        glfwShowWindow(window);
    }

    private void createModel() {
        vert2 = new float[]{
                //vx vy vz l nx ny nz
                +0.0f, +0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f,
                +0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f
        };
    }


    private void createAndCompileShaders() {
        String renderVertexShaderSrc = createShader("teststruct2Render.vert");
        String renderFragmentShaderSrc = createShader("teststruct2Render.frag");
        String geoVertexShaderSrc = createShader("tree.vert");
        String geoGeometyShaderSrc = createShader("tree.geom");

        //Compile renderVertexShader:
        renderVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(renderVertexShader, renderVertexShaderSrc);
        glCompileShader(renderVertexShader);
        if (glGetShaderi(renderVertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile renderVertexShader:\n" + glGetShaderInfoLog(renderVertexShader, 512));
        }
        //Compile renderFragmentShader:
        renderFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(renderFragmentShader, renderFragmentShaderSrc);
        glCompileShader(renderFragmentShader);
        if (glGetShaderi(renderFragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile renderFragmentShader: \n" + glGetShaderInfoLog(renderFragmentShader, 512));
        }
        //Compile renderVertexShader:
        geoVertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(geoVertexShader, geoVertexShaderSrc);
        glCompileShader(geoVertexShader);
        if (glGetShaderi(geoVertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geoVertexShader: \n" + glGetShaderInfoLog(geoVertexShader, 512));
        }
        //Compile geoVertexShader:
        geoGeometyShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geoGeometyShader, geoGeometyShaderSrc);
        glCompileShader(geoGeometyShader);
        if (glGetShaderi(geoGeometyShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geoVertexShader: \n" + glGetShaderInfoLog(geoGeometyShader, 512));
        }
    }

    private String createShader(String shaderName) {
        try {
//            System.out.println("src/main/java/" + shaderName);
            return new String(Files.readAllBytes(Paths.get("src/main/java/" + shaderName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create Shader " + shaderName);
        }
        return null;
    }

    private void createProgrammAndLinkShaders() {
        renderProgram = glCreateProgram();
        glAttachShader(renderProgram, renderVertexShader);
        glAttachShader(renderProgram, renderFragmentShader);
        glLinkProgram(renderProgram);

        geoProgram = glCreateProgram();
        glAttachShader(geoProgram, geoVertexShader);
        glAttachShader(geoProgram, geoGeometyShader);
        varyings = new CharSequence[]{"out_position", "out_length", "out_normal"};
        glTransformFeedbackVaryings(geoProgram, varyings, GL_INTERLEAVED_ATTRIBS);
        glLinkProgram(geoProgram);

    }

    private void createVertexAttribAndPointers() {
        pos = glGetAttribLocation(geoProgram, "position");
        glEnableVertexAttribArray(pos);
        glVertexAttribPointer(pos, 3, GL_FLOAT, false, 7 * sizeOfFloat, 0 * sizeOfFloat);

        length = glGetAttribLocation(geoProgram, "length");
        glEnableVertexAttribArray(length);
        glVertexAttribPointer(length, 1, GL_FLOAT, false, 7 * sizeOfFloat, 3 * sizeOfFloat);

        normal = glGetAttribLocation(geoProgram, "normal");
        glEnableVertexAttribArray(normal);
        glVertexAttribPointer(normal, 3, GL_FLOAT, false, 7 * sizeOfFloat, 4 * sizeOfFloat);
    }


    private void calculateModel() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        tree.rotate((float) Math.toRadians(1), 0f, 1f, 0f);

        tree.get(fb);

        int uniTrans = glGetUniformLocation(renderProgram, "model");
        glUniformMatrix4fv(uniTrans, false, fb);
    }

    private void calculateView() {
        view = new Matrix4f().lookAt(
                new Vector3f(0.0f, 0.0f, 7f),       //eye
                new Vector3f(0, 0, 0),            //center
                new Vector3f(0.0f, 2f, 0f)    //up
        );

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        view.get(fb);
        int uniTrans = glGetUniformLocation(renderProgram, "view");
        glUniformMatrix4fv(uniTrans, false, fb);

    }

    private void calculateProjection() {
        proj = new Matrix4f().perspective(1, 1, 3, -3);

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        proj.get(fb);
        int uniTrans = glGetUniformLocation(renderProgram, "proj");
        glUniformMatrix4fv(uniTrans, false, fb);
    }

    private void clearDisplay() {
//        glEnable(GL_DEPTH_TEST);
        glClearColor(0.78f, 0.86f, 0.83f, 1.0f);
        //        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void main(String[] args) {
        new Main();
    }

}