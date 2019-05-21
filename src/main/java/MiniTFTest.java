import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.FloatBuffer;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_INTERLEAVED_ATTRIBS;
import static org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN;
import static org.lwjgl.opengl.GL30.glBeginTransformFeedback;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glEndTransformFeedback;
import static org.lwjgl.opengl.GL30.glTransformFeedbackVaryings;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MiniTFTest {

//    private static final int NUM_POINTS = 3;

    private int shaderProgram;
    private long window;
    private int positionLocation;
    private int normalLocation;
    private int colorLocation;
    private int textureCoordLocation;
    private int lengthLocation;

    private FloatBuffer inputData;
    private int inputVBO;

    private int feedbackObject;
    private int outputVBO;
    private int queryObject;
    private float[] model;

    private int sizeOfFloat = 4;
    private Matrix4f trans;
    private Matrix4f proj;
    private Matrix4f view;
    private boolean rotateObject = true;

    public MiniTFTest() {
        initLWJGL();
        initShader();
        initAttributes();
        initTransformFeedback();
        randomizeInputData();
    }

    private void initAttributes() {
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
    }

    private void initLWJGL() {
        if (!glfwInit()) {
            return;
        }
        initializeWindow();
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

    private void initShader() {

        shaderProgram = glCreateProgram();

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, loadFileSource("src/main/java/screen.vert"));
        glCompileShader(vertexShader);
        glAttachShader(shaderProgram, vertexShader);

        int geometryShader = glCreateShader(GL_GEOMETRY_SHADER);
        glShaderSource(geometryShader, loadFileSource("src/main/java/screen.geom"));
        glCompileShader(geometryShader);
        glAttachShader(shaderProgram, geometryShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, loadFileSource("src/main/java/screen.frag"));
        glCompileShader(fragmentShader);
        glAttachShader(shaderProgram, fragmentShader);

        //This line tells the shader which output attributes from the geometry shader
        //we want to save to the transform feedback output VBO. It's an array of varying
        //names followed by an enum controlling how we want the data to be stored.
        //GL_INTER_LEAVED_ATTRIBS tells OpenGL to put them in the same VBO ordered in
        //the way specified by the array.
        //It's very important to call this BEFORE linking the program.
        glTransformFeedbackVaryings(shaderProgram, new CharSequence[]{"outPosition"}, GL_INTERLEAVED_ATTRIBS);

        //Note that we don't even have a fragment shader for this shader program.

        glLinkProgram(shaderProgram);
        String log = glGetProgramInfoLog(shaderProgram, 65536);
        if (log.length() != 0) {
            System.out.println("Program link log:\n" + log);
        }

        //Save the input variable location
        positionLocation = glGetAttribLocation(shaderProgram, "position");
        normalLocation = glGetAttribLocation(shaderProgram, "normal");
        colorLocation = glGetAttribLocation(shaderProgram, "color");
        textureCoordLocation = glGetAttribLocation(shaderProgram, "texCoord");
        lengthLocation = glGetAttribLocation(shaderProgram, "length");

    }

    private void initTransformFeedback() {

        model = new float[]{
                //Position3            //Normale3           //color3            //TexCoord2     //length1
                -0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 5.0f,
                0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 5.0f,
                0.0f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 5.0f
        };

//        model = new float[]{
//                //Position3
//                -0.5f, -0.5f, 0.0f,
//                0.5f, -0.5f, 0.0f,
//                0.0f, 0.5f, 0.0f
//        };

        //This is the buffer we fill with random points to process.
        inputData = BufferUtils.createFloatBuffer(model.length);
        //And the VBO which we upload the data to.
        inputVBO = glGenBuffers();

        //This is the data in which the processed points will end up.
        //We make it big enough to fit all input points, in case all
        //of them pass. If the buffer is filled, additional data will
        //simply be discarded.
        outputVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, outputVBO);
        glBufferData(GL_ARRAY_BUFFER, model.length, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //We create our transform feedback object. We then bind it and
        //tell it to store its output into outputVBO.
        feedbackObject = glGenTransformFeedbacks();
        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackObject);
        glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, outputVBO);
        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, 0);

        //We also create a query object. This object will be used to
        //query how many points that were stored in the output VBO.
        queryObject = glGenQueries();
    }

    //Fills the input data buffer with random point data,
    //with coordinates ranging from -1 to 1 (the whole screen).
    private void randomizeInputData() {

        if (rotateObject) {
            trans.rotate((float) Math.toRadians(1), 0f, 1f, 0f);
        }
        Random r = new Random();

//        for(int i = 0; i < NUM_POINTS; i++){
////            inputData.put(r.nextFloat() * 2 - 1).put(r.nextFloat() * 2 - 1).put(r.nextFloat() * 2 - 1);
//            inputData.put(1f/i).put(2f/i).put(0);
//        }
        inputData.put(model);

        inputData.flip();
    }

    //Loads shader source code from a file.
    public static String loadFileSource(String path) {

        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Unable to open file " + file.getAbsolutePath() + "!!!");
            return null;
        }

        StringBuilder source = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((line = reader.readLine()) != null) {
                source.append(line).append('\n');
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Failed to read shader source!");
            e.printStackTrace();
            return null;
        }
        return source.toString();
    }

    public void gameloop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);

//            randomizeInputData();
            calculateModel();
            calculateView();
            calculateProjection();

            processPoints();
            renderOutput();


            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void processPoints() {

        //Disable pixel rendering, we're doing transform feedback baby!
        glEnable(GL_RASTERIZER_DISCARD);

        //Bind the shader...
        glUseProgram(shaderProgram);

        //and then the feedback object
        glBindTransformFeedback(GL_TRANSFORM_FEEDBACK, feedbackObject);
        glBeginTransformFeedback(GL_POINTS);
        {
            //Between glBeginTransformFeedback(GL_POINTS) and glEndTransformFeedback()
            //we can of course only draw points.

            //Bind and update the input data VBO.
            glBindBuffer(GL_ARRAY_BUFFER, inputVBO);
            glBufferData(GL_ARRAY_BUFFER, inputData, GL_STREAM_DRAW);

            glEnableVertexAttribArray(positionLocation);
            glVertexAttribPointer(positionLocation, 3, GL_FLOAT, false, 12 * sizeOfFloat, 0 * sizeOfFloat);

            glEnableVertexAttribArray(normalLocation);
            glVertexAttribPointer(normalLocation, 3, GL_FLOAT, false, 12 * sizeOfFloat, 3 * sizeOfFloat);

            glEnableVertexAttribArray(colorLocation);
            glVertexAttribPointer(colorLocation, 3, GL_FLOAT, false, 12 * sizeOfFloat, 6 * sizeOfFloat);

            glEnableVertexAttribArray(textureCoordLocation);
            glVertexAttribPointer(textureCoordLocation, 2, GL_FLOAT, false, 12 * sizeOfFloat, 9 * sizeOfFloat);

            glEnableVertexAttribArray(lengthLocation);
            glVertexAttribPointer(lengthLocation, 1, GL_FLOAT, false, 12 * sizeOfFloat, 11 * sizeOfFloat);

            //Draw the points with a standard glDrawArrays() call, but wrap it in
            //a query so we can determine exactly how many points that were stored
            //in outputVBO.
            //WARNING: Querying is VERY SLOW and is only done so we can write out
            //how many points that passed to the console! It's possible to draw
            //all points that passed without a query! See renderOutput()!
            glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, queryObject);
            glDrawArrays(GL_POINTS, 0, model.length);
            glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);
            System.out.println("Points drawn: " + glGetQueryObjecti(queryObject, GL_QUERY_RESULT));

            //Clean up after us...
            glDisableVertexAttribArray(positionLocation);
            glDisableVertexAttribArray(normalLocation);
            glDisableVertexAttribArray(colorLocation);
            glDisableVertexAttribArray(textureCoordLocation);
            glDisableVertexAttribArray(lengthLocation);
            glBindBuffer(GL_ARRAY_BUFFER, 0);

        }
        glEndTransformFeedback();

        glUseProgram(0);

        glDisable(GL_RASTERIZER_DISCARD);
    }

    private void renderOutput() {

        //Bind the outputVBO just like any other VBO
        glBindBuffer(GL_ARRAY_BUFFER, outputVBO);

        //We're using the fixed functionality pipeline here,
        //so just set it up to read positions from the outputVBO.
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 12, 0);

        //glDrawTransformFeedback is a special draw call that is very similar to
        //glDrawArrays(). It allows us to draw all points that were output by our
        //shader without having to involve the CPU to determine how many they were.
        //This is the same as glDrawArrays(GL_POINTS, 0, num_points_that_passed);,
        //but is a LOT faster than if we had used a query object to get the count
        //and then manually calling glDrawArrays() with that count.
        glDrawTransformFeedback(GL_POINTS, feedbackObject);

        //Clean up...
        glDisableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

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
        if (rotateObject) {
            trans.rotate((float) Math.toRadians(1), 0f, 1f, 0f);
        }

        trans.get(fb);

        int uniTrans = glGetUniformLocation(shaderProgram, "model");
        glUniformMatrix4fv(uniTrans, false, fb);
    }

    public static void main(String[] args) {
        new MiniTFTest().gameloop();
    }
}