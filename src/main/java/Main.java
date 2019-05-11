import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClear;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glActiveTexture;
import static org.lwjgl.opengl.GL20.glEnable;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private boolean running; //game runs until running is set to false
    private long window;
    private int uniColor;
    private float pulseColor = 0;
    private boolean pulseUp = true;
    private final int sizeOfFloat = 4;
    int[] elements;
    private int shaderProgram;
    private Matrix4f trans;
    private Matrix4f proj;
    private Matrix4f view;

    public Main() {
        running = true;
        init();
        while (running) {
            update();
            render();
            if (glfwWindowShouldClose(window)) {
                running = false;
                glfwTerminate();
                break;

            }
        }

    }

    private void update() {
        glfwPollEvents();
//        calculatePulseColor();

        calculateModel();
        calculateView();
        calculateProjection();
    }

    private void calculateProjection() {
        proj = new Matrix4f().perspective(1, 1, 4, -4);

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        proj.get(fb);
        int uniTrans = glGetUniformLocation(shaderProgram, "proj");
        glUniformMatrix4fv(uniTrans,false,fb);
    }

    private void calculateView() {
        view = new Matrix4f().lookAt(
                new Vector3f(0.0f, 0.0f, 5f),   //eye
                new Vector3f(0, 0, 0),            //center
                new Vector3f(0.0f, 0.5f, 0.0f)    //up
        );

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        view.get(fb);
        int uniTrans = glGetUniformLocation(shaderProgram, "view");
        glUniformMatrix4fv(uniTrans,false,fb);

    }

    private void calculateModel() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        trans.rotate((float) Math.toRadians(1),1f,0.5f,0.25f);
        trans.get(fb);

        int uniTrans = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(uniTrans,false,fb);
    }

    private void calculatePulseColor() {
        if (pulseUp) {
            pulseColor += 0.01f;
            if (pulseColor >= 1.0f) {
                pulseUp = false;
            }
        } else {
            pulseColor -= 0.01f;
            if (pulseColor <= 0.0f) {
                pulseUp = true;
            }
        }
    }

    private void render() {
        clearDisplay();

        glUniform3f(uniColor, pulseColor, 1 - pulseColor, 0.25f + 0.5f * pulseColor);
        actuallyDraw();

    }

    private void actuallyDraw() {
        glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_INT, 0);
        glfwSwapBuffers(window);
    }

    private void clearDisplay() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
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


        float[] model = {
                //  Position3  Color3         Texcoords2
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // Top-left
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Top-right
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // Bottom-right
                -0.5f, -0.5f, 0.5f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,  // Bottom-left

                -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // Top-left
                0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Top-right
                0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // Bottom-right
                -0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,  // Bottom-left


        };
        //<editor-fold desc="VAO Stuff">
        //Creating a VertexArrayObject
        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // Create a Vertex Buffer Object and copy the vertex data to it
        int vbo = GL15.glGenBuffers();
        //Create vertex buffer
        FloatBuffer verticeBuffer = BufferUtils.createFloatBuffer(model.length);
        verticeBuffer.put(model).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //Send vertice buffer to VBO
        glBufferData(GL_ARRAY_BUFFER, verticeBuffer, GL_STATIC_DRAW);
        //</editor-fold>


        //<editor-fold desc="EBO stuff">
        //Create Element Buffer Object:
        elements = new int[]{
                0, 3, 1,
                1, 3, 2,    //front pane
                1, 2, 5,
                5, 2, 6,    //back Pane
                4, 7, 0,
                0, 7, 3,    //left pane
                0, 1, 4,
                4, 1, 5,    //top Pane
                7, 6, 3,
                3, 6, 2,    //bottom Pane
                5, 6, 4,
                4, 6, 7     //right Pane
        };
        //Creating a ElementBufferObject
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elements.length);
        elementBuffer.put(elements).flip();

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
        //</editor-fold>


        //<editor-fold desc="Shader Stuff">
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
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glBindFragDataLocation(shaderProgram, 0, "outColor");
        glLinkProgram(shaderProgram);
        glUseProgram(shaderProgram);
        //</editor-fold>


        //<editor-fold desc="Attrib Pointer Stuff">
        int posAttrib = glGetAttribLocation(shaderProgram, "position");
        glEnableVertexAttribArray(posAttrib);
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 8 * sizeOfFloat, 0);

        int colAttrib = glGetAttribLocation(shaderProgram, "color");
        glEnableVertexAttribArray(colAttrib);
        glVertexAttribPointer(colAttrib, 3, GL_FLOAT, false, 8 * sizeOfFloat, 3 * sizeOfFloat);

        int texAttrib = glGetAttribLocation(shaderProgram, "texcoord");
        glEnableVertexAttribArray(texAttrib);
        glVertexAttribPointer(texAttrib, 2, GL_FLOAT, false, 8 * sizeOfFloat, 6 * sizeOfFloat);
        //</editor-fold>


        //<editor-fold desc="newTextures">
        Texture texture = new Texture("treebark.jpg");
        int texUnit = 0;
        int texUniform = glGetUniformLocation(shaderProgram, "tex");
        glUniform1i(texUniform, texUnit);
        glActiveTexture(GL_TEXTURE0 + 5);  //+5!!!
        texture.bind();
        //</editor-fold>

        //create MVP:
        trans = new Matrix4f().identity();
        proj = new Matrix4f().identity();
        view = new Matrix4f().identity();




        uniColor = glGetUniformLocation(shaderProgram, "triangleColor");
        glUniform3f(uniColor, 1.0f, 0.0f, 0.0f);

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
        glfwSetWindowPos(window, (vidmode != null ? vidmode.width() : 0) / 2 - w / 2, (vidmode != null ? vidmode.height() : 0) / 2 - h / 2);
        glfwMakeContextCurrent(window);
        createCapabilities();
        glfwShowWindow(window);

        //clear the window to be black:
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glEnable(GL_TEXTURE_3D);

    }

    public static void main(String[] args) {
        new Main();
    }

}