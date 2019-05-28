import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_STATIC_READ;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindBuffer;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL20.glBufferSubData;
import static org.lwjgl.opengl.GL20.glClear;
import static org.lwjgl.opengl.GL20.glClearColor;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDisable;
import static org.lwjgl.opengl.GL20.glDrawArrays;
import static org.lwjgl.opengl.GL20.glEnable;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glFlush;
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
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Testing implements Runnable {
    private int windowWidth = 800;
    private int windowHeight = 640;
    private int iterations = 7;
    private int numberOfVertices;

    private GLFWErrorCallback errorCallback;
    private long window;
    private int vao;
    private int vboPosition;
    private float[] model;
    private int myBufferTriangle;
    private int constructionProgram;
    private int renderProgram;
    private int myBufferFeedback;
    private int vertexArr;
    private int currentVertexArray;
    private int currentFeedbackBuffer;
    private int renderVertex;
    private int lastVertexArray;
    private int lastFeedbackBuffer;
    private Matrix4f proj;
    private Matrix4f view;
    private Matrix4f tree;
    private int swapVertexArray;
    private int swapFeedbackBuffer;

    /* Main Funktion */
    public static void main(String[] args) {
        new Testing().run();
    }

    /* Run Methode: Damit wird das Fenster gestartet.
     * Hier wird auch der Frame Loop definiert
     */
    @Override
    public void run() {
        initWindow();
        GL.createCapabilities();
        initShaders();
        initModel();
        numberOfVertices = calculateVertices(iterations);
        generateConstructionBuffer();
        createConstructionVertexArrayObject();
        createConstructionAttribPionters();
        generateRenderBuffer();
        createRenderVertexArrayObject();
        createRenderAttribPionters();
        setPointers();
        buildTree();
        clearColor();
//
//        gameloop();

        terminateApplication();


    }
    private void terminateApplication() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        // Terminate GLFW and free the error callback
        glfwTerminate();
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
    private void gameloop() {
        while (!glfwWindowShouldClose(window)) {
            glClear (GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glBindVertexArray(renderVertex);
            glUseProgram(renderProgram);
            glDrawArrays(GL_TRIANGLES,0,numberOfVertices);
            glfwPollEvents();
            glfwSwapBuffers(window);
            updateMatrices();
            calculateModel();
            calculateView();
            calculateProjection();


        }
    }

    private void clearColor() {
        glClearColor(0.78f, 0.86f, 0.83f, 1.0f);
    }

    private void buildTree() {
        for (int i = 0; i < iterations; ++i) {
            glBindVertexArray(currentVertexArray);
            glUseProgram(constructionProgram);
            glBindBuffer(GL_ARRAY_BUFFER, currentFeedbackBuffer);
            glEnable(GL_RASTERIZER_DISCARD);
            glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER,0,currentFeedbackBuffer);
            glBeginTransformFeedback(GL_TRIANGLES);
            glDrawArrays(GL_TRIANGLES,0,numberOfVertices);
            glEndTransformFeedback();
            glDisable(GL_RASTERIZER_DISCARD);
            glFlush();

            swapVertexArray = glGenVertexArrays();
            swapVertexArray = currentVertexArray;
            currentVertexArray = lastVertexArray;
            lastVertexArray = swapVertexArray;

            swapFeedbackBuffer = glGenBuffers();
            swapFeedbackBuffer = currentFeedbackBuffer;
            currentFeedbackBuffer = lastFeedbackBuffer;
            lastFeedbackBuffer = swapFeedbackBuffer;
        }
    }

    private void setPointers() {
        currentVertexArray = glGenVertexArrays();
        currentVertexArray = vertexArr;
        currentFeedbackBuffer = glGenBuffers();
        currentFeedbackBuffer = myBufferFeedback;

        lastVertexArray = glGenVertexArrays();
        lastVertexArray = renderVertex;
        lastFeedbackBuffer = glGenBuffers();
        lastFeedbackBuffer = myBufferTriangle;

    }

    private void createRenderAttribPionters() {
        int renderPos = glGetAttribLocation(renderProgram, "position");
        glEnableVertexAttribArray(renderPos);
        glVertexAttribPointer(renderPos, 3, GL_FLOAT, false, 7 * 4, 0 * 4);

        int renderLength = glGetAttribLocation(renderProgram, "length");
        glEnableVertexAttribArray(renderLength);
        glVertexAttribPointer(renderLength, 1, GL_FLOAT, false, 7 * 4, 3 * 4);

        int renderNormal = glGetAttribLocation(renderProgram, "normal");
        glEnableVertexAttribArray(renderNormal);
        glVertexAttribPointer(renderNormal, 3, GL_FLOAT, false, 7 * 4, 4 * 4);
    }

    private void createRenderVertexArrayObject() {
        renderVertex = glGenVertexArrays();
        glBindVertexArray(renderVertex);
        glBindBuffer(renderVertex, myBufferFeedback);
    }

    private void generateRenderBuffer() {
        myBufferFeedback = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, myBufferFeedback);
        glBufferData(myBufferTriangle, model.length * numberOfVertices, GL_STATIC_READ);
        //no sub data
    }

    private void createConstructionAttribPionters() {
        int pos = glGetAttribLocation(constructionProgram, "position");
        glEnableVertexAttribArray(pos);
        glVertexAttribPointer(pos, 3, GL_FLOAT, false, 7 * 4, 0 * 4);

        int length = glGetAttribLocation(constructionProgram, "length");
        glEnableVertexAttribArray(length);
        glVertexAttribPointer(length, 1, GL_FLOAT, false, 7 * 4, 3 * 4);

        int normal = glGetAttribLocation(constructionProgram, "normal");
        glEnableVertexAttribArray(normal);
        glVertexAttribPointer(normal, 3, GL_FLOAT, false, 7 * 4, 4 * 4);
    }

    private void createConstructionVertexArrayObject() {
        vertexArr = glGenVertexArrays();
        glBindVertexArray(vertexArr);
        glBindBuffer(vertexArr, myBufferTriangle);
    }

    private void generateConstructionBuffer() {
        myBufferTriangle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, myBufferTriangle);

        glBufferData(myBufferTriangle, model.length * numberOfVertices, GL_STATIC_DRAW);
        glBufferSubData(myBufferTriangle, 0, model);
    }

    private void initModel() {
        model = new float[]{
                +0.0f, +0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f,
                +0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f,
        };
    }

    private void initShaders() {
        //RENDERING SHADERS:
        //create and compile renderVertexShader:
        int renderVertexShader = createAndCompileShader(GL_VERTEX_SHADER, "renderShader.vert");
        //create and compile renderFragmentShader:
        int renderFragmentShader = createAndCompileShader(GL_FRAGMENT_SHADER, "renderShader.frag");
        //create renderProgram:
        renderProgram = createProgramAndAttachShaders(renderVertexShader, renderFragmentShader);
        //link program:
        glLinkProgram(renderProgram);

        //CONSTRUCTION SHADERS:
        int constructionVertexShader = createAndCompileShader(GL_VERTEX_SHADER, "constructionShader.vert");
        int constructionGeometryShader = createAndCompileShader(GL_GEOMETRY_SHADER, "constructionShader.geom");
        constructionProgram = createProgramAndAttachShaders(constructionVertexShader, constructionGeometryShader);
        //before linking, we have to implement transform feedback varyings:
        CharSequence[] varyings = new CharSequence[]{"out_position", "out_length", "out_normal"};
        glTransformFeedbackVaryings(constructionProgram, varyings, GL_INTERLEAVED_ATTRIBS);
        glLinkProgram(constructionProgram);
    }

    private int createProgramAndAttachShaders(int renderVertexShader, int renderFragmentShader) {
        int program = glCreateProgram();
        glAttachShader(program, renderVertexShader);
        glAttachShader(program, renderFragmentShader);
        return program;
    }

    private int createAndCompileShader(int shaderType, String shaderName) {
        //CREATE SHADER:
        String shaderSource = "";
        try {
//            System.out.println("src/main/java/" + shaderName);
            shaderSource = new String(Files.readAllBytes(Paths.get("src/main/java/" + shaderName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create Shader " + shaderName);
        }
        //COMPILE SHADER:
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, shaderSource);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile renderVertexShader:\n" + glGetShaderInfoLog(shader, 512));
        }

        return shader;

    }

    private void initWindow() {
        System.out.println("LWJGL version: " + Version.getVersion());
        glfwSetErrorCallback(errorCallback = GLFWErrorCallback
                .createPrint(System.err));

        if (glfwInit() != true)
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        window = glfwCreateWindow(windowWidth, windowHeight, "Demo", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        glfwShowWindow(window);
    }

    private int calculateVertices(int numberOfIterations) {
        int tempTriangles = (int) (4 * Math.pow(3, numberOfIterations) - 3);
        return tempTriangles * 3;
    }
}
