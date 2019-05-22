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
import static org.lwjgl.opengl.GL15.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glClear;
import static org.lwjgl.opengl.GL15.glClearColor;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL20.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL20.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_FLOAT;
import static org.lwjgl.opengl.GL20.GL_TEXTURE0;
import static org.lwjgl.opengl.GL20.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL20.GL_TRIANGLES;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL20.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDrawElements;
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
    private int triangles = 3;
    private int sizeOfFloat = 4;
    private float[] model;
    private int vao;
    private int vbo;
    private int vertexShader;
    private int geometryShader;
    private int fragmentShader;
    private int posAttrib;
    private int normalAttrib;
    private int colorAttrib;
    private int texAttrib;
    private int lengthAttrib;

    public Main() {
        init();
//        calculateTest();
        while (true) {
            update();
            render();
            if (glfwWindowShouldClose(window)) {
                glfwTerminate();
                System.exit(1);
                break;
            }
        }

    }

    private void init() {

        if (!glfwInit()) {
            return;
        }
        initializeWindow();
        createMVP();
        createModel();
        createVAO();
        createVBO();
        createEBO();
        createAndCompileShaders();
        createProgrammAndLinkShaders();
        createVertexAttribAndPointers();
        createAndSetUpTexture();
        bindFragmentDataLocation();
        linkProgram();
        createTriangleColorUniform();
        useProgram();
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

    private void createMVP() {
        //create MVP:
        trans = new Matrix4f().identity();
        trans.rotate((float) Math.toRadians(30), 1f, 0f, 0);
        proj = new Matrix4f().identity();
        view = new Matrix4f().identity();
    }

    private void createModel() {
        model = new float[]{
                //Position3            //Normale3           //color3            //TexCoord2     //length1
                -0.3f, -0.7f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 5.0f,
                0.0f, -0.7f, 0.3f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 5.0f,
                0.3f, -0.7f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 5.0f
        };
    }

    private void createVAO() {
        //Creating a VertexArrayObject
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);


    }

    private void createVBO() {
        // Create a Vertex Buffer Object and copy the vertex data to it
        vbo = GL15.glGenBuffers();
        //Create vertex buffer
        FloatBuffer verticeBuffer = BufferUtils.createFloatBuffer(model.length);
        verticeBuffer.put(model).flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        //Send vertice buffer to VBO
        glBufferData(GL_ARRAY_BUFFER, verticeBuffer, GL_STATIC_DRAW);
    }

    private void createEBO() {
        elements = new int[]{
                0, 1, 2
        };
        //Creating a ElementBufferObject
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elements.length);
        elementBuffer.put(elements).flip();

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
    }

    private void createAndCompileShaders() {
        String vertexSource = createVertexShader();
        String geometrySource = createGeometryShader();
        String fragmentSource = createFragmentShader();
        //Compile vertexShader:
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile vertexShader:\n" + glGetShaderInfoLog(vertexShader, 512));
        }
        //Compile geometryShader:
        geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, geometrySource);
        glCompileShader(geometryShader);
        if (glGetShaderi(geometryShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile geometryShader: \n" + glGetShaderInfoLog(geometryShader, 512));
        }
        //Compile fragmentShader:
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE) {
            System.err.println("couldn't compile fragmentShader: \n" + glGetShaderInfoLog(fragmentShader, 512));
        }
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

    private String createFragmentShader() {
        try {
            return new String(Files.readAllBytes(Paths.get("src/main/java/screen.frag")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("couldn't create fragmentShader");
        }
        return null;
    }

    private void createProgrammAndLinkShaders() {
        // Link the vertex, geometry and fragment shader into a shader program
        shaderProgram = glCreateProgram();

        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, geometryShader);
        glAttachShader(shaderProgram, fragmentShader);
    }

    private void createVertexAttribAndPointers() {
        posAttrib = glGetAttribLocation(shaderProgram, "pos");
        glEnableVertexAttribArray(posAttrib);
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 12 * sizeOfFloat, 0 * sizeOfFloat);
        normalAttrib = glGetAttribLocation(shaderProgram, "normal");
        glEnableVertexAttribArray(normalAttrib);
        glVertexAttribPointer(normalAttrib, 3, GL_FLOAT, false, 12 * sizeOfFloat, 3 * sizeOfFloat);
        colorAttrib = glGetAttribLocation(shaderProgram, "color");
        glEnableVertexAttribArray(colorAttrib);
        glVertexAttribPointer(colorAttrib, 3, GL_FLOAT, false, 12 * sizeOfFloat, 6 * sizeOfFloat);
        texAttrib = glGetAttribLocation(shaderProgram, "texcoord");
        glEnableVertexAttribArray(texAttrib);
        glVertexAttribPointer(texAttrib, 2, GL_FLOAT, false, 12 * sizeOfFloat, 9 * sizeOfFloat);
        lengthAttrib = glGetAttribLocation(shaderProgram, "length");
        glEnableVertexAttribArray(lengthAttrib);
        glVertexAttribPointer(lengthAttrib, 1, GL_FLOAT, false, 12 * sizeOfFloat, 11 * sizeOfFloat);
    }

    private void createAndSetUpTexture() {
        Texture texture = new Texture("treebark.jpg");
        int texUnit = 0;
        int texUniform = glGetUniformLocation(shaderProgram, "tex");
        glUniform1i(texUniform, texUnit);
        glActiveTexture(GL_TEXTURE0 + 5);  //+5!!!
        texture.bind();
    }

    private void bindFragmentDataLocation() {
        glBindFragDataLocation(shaderProgram, 0, "outColor");
    }

    private void linkProgram() {
        glLinkProgram(shaderProgram);
    }

    private void createTriangleColorUniform() {
        uniColor = glGetUniformLocation(shaderProgram, "triangleColor");
        glUniform3f(uniColor, 1.0f, 0.0f, 0.0f);
    }

    private void useProgram() {
        glUseProgram(shaderProgram);
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

    private void update() {
        glfwPollEvents();
        calculatePulseColor();
        calculateModel();
        calculateView();
        calculateProjection();
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

    private void calculateModel() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        if (rotateObject) {
            trans.rotate((float) Math.toRadians(1), 0f, 1f, 0f);
        }

        trans.get(fb);

        int uniTrans = glGetUniformLocation(shaderProgram, "model");
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

    private void calculateProjection() {
        proj = new Matrix4f().perspective(1, 1, 3, -3);

        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        proj.get(fb);
        int uniTrans = glGetUniformLocation(shaderProgram, "proj");
        glUniformMatrix4fv(uniTrans, false, fb);
    }


    private void render() {
        clearDisplay();
        glUniform3f(uniColor, pulseColor, 1 - pulseColor, 0.25f + 0.5f * pulseColor);
        glDrawElements(GL_TRIANGLES, elements.length, GL_UNSIGNED_INT, 0 * triangles);
        glfwSwapBuffers(window);

    }

    private void clearDisplay() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public static void main(String[] args) {
        new Main();
    }

}