import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Einfuehrung in die Computergrafik
 *
 * @author F. N. Rudolph, V. Baguio (c) 2016
 *         12.04.2016
 */
public class DreieckUebung implements Runnable {

    /* Main Funktion */
    public static void main(String[] args) {
        new DreieckUebung().run();
    }

    /* Attribute */
    private long window;
    private GLFWErrorCallback errorCallback;
    private int WIDTH = 600;
    private int HEIGHT = 600;

    // ZU ALLER ERST
    private int program;
    private int vao = -1;
    private int vboPosition = -1;
    private int vboColor = -1;
    private int vertexShader;
    private int fragmentShader;

    /* Constructor */
    public DreieckUebung() {

    }

    /* Run Methode: Damit wird das Fenster gestartet.
     * Hier wird auch der Frame Loop definiert
     */
    @Override
    public void run() {
        try{
            initWindow();
            GL.createCapabilities();

            initBuffers();
            initShaders();

            // Game Loop
            while (!glfwWindowShouldClose(window)) {
                initBuffers();
                frame();
                glfwSwapBuffers(window);
                glfwPollEvents();
            }

        }
        finally {
            // IRGENDWANN!!!
            glfwHideWindow(window);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(program);
            System.exit(0);
        }
    }

    private void frame() {
        glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        /* Schritt 3 */
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 3);
    }

    private void initBuffers() {
        // Schritt 5: Clean up!
        if (vao > -1) {
            glDeleteBuffers(vao);
            glDeleteVertexArrays(vboPosition);
            glDeleteVertexArrays(vboColor);
        }

        /* Schritt 1: Die Buffer initialisieren */

        // Vertex Array Object Erstellen
        vao = glGenVertexArrays();
        // vao auswählen
        glBindVertexArray(vao);

        // Homogene Koordinaten x, y, z, w=1
        vboPosition = glGenBuffers(); //GL15
        float[] positions = new float[]{
                -0.5f, -0.5f, 0.0f, 1.0f,
                -0.5f, 0.5f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.0f, 1.0f
        };

        // auf GPU hochladen
        FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(positions.length);
        dataBuffer.put(positions);
        dataBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboPosition);
        glBufferData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW);

        // Fehler abfragen lassen
//        GLUtil.checkGLError();

        // enable vertex attribute in_pos
        glEnableVertexAttribArray(0); //index for position

        // tell, where exactly in the array you can find this attribute in the binded vbo
        // index: basically the "name" of this attribute, to connect to the program
        // size: how many components?
        // type: what kind of components?
        // normalized?
        // stride: Stride is the distance from the beginning of one attrib,
        // to the beginning of the following attrib. 0 means tightly packed!
        // pointer offset: from the very beginning, where does this attrib start? in byte
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0L);

//        GLUtil.checkGLError();
        /* Für jede Information oder VBO, dass man verwenden will
         * muss man das ganze Gedöns einmal machen!
         * VBO generieren, Buffer generieren, Daten abspeichern, Flip
         * Dann
         * Binden, hochladen, unbinden!
         */

        /* Schritt 4 : Farben dazu nehmen */
        vboColor = glGenBuffers();
        float[] colors = new float[]{
                1f, 1f, 0f, 1f,
                1f, 0f, 1f, 1f,
                0f, 1f, 1f, 1f,
        };
        dataBuffer = BufferUtils.createFloatBuffer(colors.length);
        dataBuffer.put(colors);
        dataBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboColor);
        glBufferData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW);
//        GLUtil.checkGLError();
        glEnableVertexAttribArray(1); // index for color
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0L);
//        GLUtil.checkGLError();
    }


    /**
     * Shader definieren
     */
    private void initShaders() {
        /* Schritt 2 */

        // The Shader Code in a string
        String vert=
                "#version 330					\n"+ //specifies GLSL version
                        "in vec4 in_pos;				\n"+ //entering data (data array)
                        "in vec4 in_color;              \n"+ // später dazu
                        "out vec4 var_color;            \n"+ // varying erklären
                        "void main(){					\n"+
                        "    gl_Position = in_pos;		\n"+ //set position
                        "    var_color = in_color;      \n"+ // pass through to fragment shader
                        "}								\n"; //gl_pos will be passed thru the pipeline
        // Create the Vertex Shader
        int vertexShader = createShader(GL_VERTEX_SHADER, vert);

        String frag=
                "#version 330                                	   \n"+
                        "out vec4 out_color;                      		   \n"+
                        "in vec4 var_color;                                \n"+
                        "void main(){		                  			   \n"+
                        //"    out_color = vec4(0.0f, 1.0f, 1.0f, 1.0f);     \n"+ //output cyan
                        "    out_color = var_color;                        \n"+
                        "}                                         		   \n";
        // Create the Fragment shader
        int fragmentShader = createShader(GL_FRAGMENT_SHADER, frag);

        // Programm erstellen
        program = glCreateProgram();
        glAttachShader(program, vertexShader); //shader attachen
        glAttachShader(program, fragmentShader); //shader attachen

        // Shader zeigen, wo die Attribute herkommen
        glBindAttribLocation(program, 0, "in_pos"); //gleicher index wie in initBuffers();
        glBindAttribLocation(program, 1, "in_color");
//        GLUtil.checkGLError();

        glLinkProgram(program); //programm linken
//        GLUtil.checkGLError();



        // use the program
        glUseProgram(program);
//        GLUtil.checkGLError();
    }

    /**
     * Fenster initialisieren
     */
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

        window = glfwCreateWindow(WIDTH, HEIGHT, "Demo", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        glfwShowWindow(window);
    }

    /**
     * Creates a shader, compiles and checks for compile errors
     * @param shadertype
     * @param shaderSource
     * @return
     */
    private int createShader(int shadertype, String shaderSource){
        // Create a Shader Object in GPU and give me the int for it
        // (see handle explanation above)
        int shader = glCreateShader(shadertype);

        // Upload our source string to the GPU in the specified shader
        glShaderSource(shader, shaderSource);

        // try to compile the shader
        glCompileShader(shader);

        // check the status for errors
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE){
            // Following code prints the info into the error stream
            String error = glGetShaderInfoLog(shader);
            String shaderTypeString = null;
            switch(shadertype)
            {
                case GL_VERTEX_SHADER: shaderTypeString = "vertex"; break;
                case GL_FRAGMENT_SHADER: shaderTypeString = "fragment"; break;
            }
            System.err.println("Compile failure in " + shaderTypeString  +" shader:\n" + error);
        }

        // Lets return the shader so we can add it to the program
        return shader;
    }
}