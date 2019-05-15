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
import java.util.Timer;
import java.util.TimerTask;

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
import static org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL20.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.glActiveTexture;
import static org.lwjgl.opengl.GL20.glEnable;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;
    private int uniColor;
    private float pulseColor = 1;
    private boolean pulseUp = true;
    private int[] elements;
    private int shaderProgram;
    private Matrix4f trans;
    private Matrix4f proj;
    private Matrix4f view;
    private int test = 0;
    private boolean testUp = true;
    private boolean rotateObject = true;

    public Main() {
        //game runs until running is set to false
        boolean running = true;
        init();
//        calculateTest();
        while (true) {
            //TESTING AREA:


            //TESTING AREA END
            update();
            render();
            if (glfwWindowShouldClose(window)) {
                running = false;
                glfwTerminate();
                System.exit(1);
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
        proj = new Matrix4f().perspective(1, 1, 3, -3);

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        proj.get(fb);
        int uniTrans = glGetUniformLocation(shaderProgram, "proj");
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
        int uniTrans = glGetUniformLocation(shaderProgram, "view");
        glUniformMatrix4fv(uniTrans, false, fb);

    }

    private void calculateModel() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        if (rotateObject){
        trans.rotate((float) Math.toRadians(1), 0f, 1f, 0f);
        }

        trans.get(fb);

        int uniTrans = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(uniTrans, false, fb);
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

        glUniform3f(uniColor, pulseColor, pulseColor, pulseColor);
        actuallyDraw();

    }

    private void actuallyDraw() {
//        System.out.println(test);
//        calculateTest();
        int triangles = 3;
//        glDrawElements(GL_TRIANGLES, 3, GL_UNSIGNED_INT, 0*triangles);
        glDrawElements(GL_POINTS, elements.length, GL_UNSIGNED_INT, 0 * triangles);
        glfwSwapBuffers(window);
    }

    private void calculateTest() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                test++;
                System.out.println(test);
            }
        }, 0, 500); // 1000 = 1 Sek.
    }

    private void clearDisplay() {
        glEnable(GL_DEPTH_TEST);
        //clear the window to be black:
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //clear the window to be white:
//        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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

    private String createGeometryShader() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/java/screen.geom")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create geometryShader");
        }
        return null;
    }

    private void init() {

        if (!glfwInit()) {
            return;
        }
        initializeWindow();

//        //cube Model:
//        float[] model = {
//                //  Position3  Color3         Texcoords2
//                //Front:
//                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // Top-left
//                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Top-right
//                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // Bottom-right
//                -0.5f, -0.5f, 0.5f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,  // Bottom-left
//                //Back:
//                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, // Top-left
//                0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, // Top-right
//                0.5f, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, // Bottom-right
//                -0.5f, -0.5f, -0.5f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f  // Bottom-left
//
//        };

        //testing:
        //tetraeder Model
        float[] model = {
                //  Position3  Color3         Texcoords2
                -0.5f, 0f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,  //vl
                0.5f, 0f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,   //vr
                0.0f, 0f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f   //h



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

//        //cube ebo:
//        elements = new int[]{
//                0, 3, 1,
//                1, 3, 2,  //front
//                4, 7, 5,
//                5, 7, 6,  //back
//                4, 0, 1,
//                4, 1, 5,  //top
//                7, 3, 2,
//                7, 2, 6,  //bottom
//                0, 3, 7,
//                0, 7, 4,  //left
//                1, 2, 6,
//                1, 6, 5   //right
//        };

        //tetraeder ebo:
        elements = new int[]{
                0,1,2
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
        String vertexSource = createVertexShader();
        String geometrySource = createGeometryShader();
        String fragmentSource = createFragmentShader();
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
        //Compile geometryShader:
        int geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, geometrySource);
        glCompileShader(geometryShader);
        if (glGetShaderi(geometryShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geometryShader: \n" + glGetShaderInfoLog(geometryShader, 512));
        }
        // Link the vertex, geometry and fragment shader into a shader program
        shaderProgram = glCreateProgram();

        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, geometryShader);
        glAttachShader(shaderProgram, fragmentShader);

        glBindFragDataLocation(shaderProgram, 0, "outColor");
        glLinkProgram(shaderProgram);
        glUseProgram(shaderProgram);
        //</editor-fold>


        //<editor-fold desc="Attrib Pointer Stuff">
        int posAttrib = glGetAttribLocation(shaderProgram, "position");
        glEnableVertexAttribArray(posAttrib);
        int sizeOfFloat = 4;
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 8 * sizeOfFloat, 0);

        int colAttrib = glGetAttribLocation(shaderProgram, "color");
        glEnableVertexAttribArray(colAttrib);
        glVertexAttribPointer(colAttrib, 3, GL_FLOAT, false, 8 * sizeOfFloat, 3 * sizeOfFloat);

        int texAttrib = glGetAttribLocation(shaderProgram, "texcoord");
        glEnableVertexAttribArray(texAttrib);
        glVertexAttribPointer(texAttrib, 2, GL_FLOAT, false, 8 * sizeOfFloat, 6 * sizeOfFloat);
        //</editor-fold>


        //<editor-fold desc="Texture Stuff">
        Texture texture = new Texture("treebark.jpg");
        int texUnit = 0;
        int texUniform = glGetUniformLocation(shaderProgram, "tex");
        glUniform1i(texUniform, texUnit);
        glActiveTexture(GL_TEXTURE0 + 5);  //+5!!!
        texture.bind();
        //</editor-fold>

        //<editor-fold desc="MVP creation">
        //create MVP:
        trans = new Matrix4f().identity();
        trans.rotate((float) Math.toRadians(30), 1f, 0f, 0);
        proj = new Matrix4f().identity();
        view = new Matrix4f().identity();
        //</editor-fold>


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


        glEnable(GL_TEXTURE_3D);


    }

    public static void main(String[] args) {
        new Main();
    }

}