import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
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
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MobiusExample implements Runnable{
    public static void main(String[] args) {
        new MobiusExample().run();
    }

    private long window;
    private int program;
    private int vao;
    private int vbo;
    private int count;
    private int lightLoc;
    private int matLoc;
    private int vertexShader;
    private int fragmentShader;

    public void run() {
        initWindow();
        GL.createCapabilities();
        initShaders();
        initBuffers();
        while (!glfwWindowShouldClose(window)) {
            frame();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
        cleanUp();
    }

    private void cleanUp() {
        glfwHideWindow(window);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        glDeleteProgram(program);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    private void initBuffers() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //Alle Daten werden in einen Buffer geladen (Position, Farbe und Normale)
        vbo = glGenBuffers();
        float[] pos = mobiusVertices(0.f,0.f,0.75f,0.5f,1000,5);
        FloatBuffer data = BufferUtils.createFloatBuffer(pos.length);
        data.put(pos);
        data.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
//        GLUtil.checkGLError();
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        //Hier muss dann immer der pointerOffset angepasst werden
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 48, 0L);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 48, 16L);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 48, 32L);
//        GLUtil.checkGLError();
    }

    /**
     * @param xm
     * 			X-Koordinate des Mittelpunktes
     * @param ym
     * 			Y-Koordiante des Mittelpunktes
     * @param r
     * 			Radius des Ringes
     * @param d
     * 			Breite des Bandes
     * @param n
     * 			Anzahl der Aussenpunkte
     * @param t
     * 			Anzahl des Drehungen des Bandes * 180 Grad	        
     * @return float Array mit abwechselnd Position, Farbe und Normale
     */
    private float[] mobiusVertices(float xm, float ym, float r, float d, int n, int t) {
        this.count = (n+1) * 2;	//Wird zum zeichnen (in frame()) global benoetigt
        //n Punkte aussen davon einer doppelt
        float[] result = new float[(n + 1) * 4 * 2 * 3];	//*4 weil xyzw/rgba
        //*2 weil immer 2 Punkte
        //*3 weil position, color und normale
        float x,y,z,sin,cos,sin2,cos2,offset,circleAngle;
        for (int i = 0; i <= n; i++)
        {
            //bei i = 0 und i = n kommt fuer "circleAngle" der gleiche Wert
            //dadurch erhalten wir den benoetigten doppelten Punkt
            circleAngle = (float)i/(float)n * 2.0f * (float)Math.PI;
            sin = (float)Math.sin(circleAngle);
            cos = (float)Math.cos(circleAngle);
            sin2 = (float)Math.sin(circleAngle * (float)t / 2.0f);
            cos2 = (float)Math.cos(circleAngle * (float)t / 2.0f);
            x = xm + cos * r;
            y = ym + sin * r;
            z = cos2 * d / 2.0f;
            offset = sin2 * d / 2.0f;

            //Punkt 1
            result[i * 24] 	   = x + cos * offset;
            result[i * 24 + 1] = y + sin * offset;
            result[i * 24 + 2] = z;
            result[i * 24 + 3] = 1.0f;	//w immer fest auf 1

            //Punkt 2
            result[i * 24 + 12]  = x - cos * offset;
            result[i * 24 + 13]  = y - sin * offset;
            result[i * 24 + 14] = -z;
            result[i * 24 + 15] = 1.0f;	//w immer fest auf 1

            //Farben (fuer beide Punkte gleich)
            result[i * 24 + 4] = result[i * 24 + 16] = 0.5f + 0.5f * sin;
            result[i * 24 + 5] = result[i * 24 + 17] = 0.5f + 0.5f * cos;
            result[i * 24 + 6] = result[i * 24 + 18] = 0.5f + 0.5f * -sin;
            result[i * 24 + 7] = result[i * 24 + 19] = 1.0f;	//alpha immer fest auf 1

            //Normalen (fuer beide Punkte gleich)
            result[i * 24 + 8]  = result[i * 24 + 20] =	cos * cos2;
            result[i * 24 + 9]  = result[i * 24 + 21] = sin * cos2;
            result[i * 24 + 10] = result[i * 24 + 22] = sin2;
            result[i * 24 + 11] = result[i * 24 + 23] = 0.0f;	//Normale haben w = 0
        }
        return result;
    }

    private void frame() {
        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glBindVertexArray(vao);
        float time = (float) glfwGetTime();
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
        //um X-Schse drehen
//		matBuffer.put(new float[]{
//			1, 0, 0, 0,
//			0,(float) Math.cos(time), (float) -Math.sin(time), 0,
//			0,(float) Math.sin(time), (float) Math.cos(time), 0,
//			0, 0, 0, 1
//		});
        //um Y-Schse drehen
        matBuffer.put(new float[]{
                (float) Math.cos(time), 0, (float) Math.sin(time), 0,
                0, 1, 0, 0,
                (float) -Math.sin(time), 0, (float) Math.cos(time), 0,
                0, 0, 0, 1
        });
        //um Z-Schse drehen
//				matBuffer.put(new float[]{
//				(float) Math.cos(time), (float) -Math.sin(time), 0, 0,
//				(float) Math.sin(time), (float) Math.cos(time), 0, 0,
//				0, 0, 1, 0,
//				0, 0, 0, 1
//			});
        matBuffer.flip();
        //gibt die von der Zeit abhaengige Matrix an den Shader
        glUniformMatrix4fv(matLoc, false, matBuffer);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, count);
    }

    private void initShaders() {
        vertexShader=glCreateShader(GL_VERTEX_SHADER);
        fragmentShader=glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vertexShader,
                "#version 330\n"
                        + "in vec4 in_pos;\n"
                        + "in vec4 in_color;\n"
                        + "in vec4 in_normal;\n"
                        + "out vec4 var_color;\n"
                        + "out vec4 var_normal;\n"
                        + "uniform mat4 u_m;\n"
                        + "void main(){\n"
                        + "//x Koordinate * 3/4 als aspect ratio des Fensters\n"
                        + "	gl_Position= u_m * vec4(in_pos.x * 3.f/4.f, in_pos.yzw);\n"
                        + " var_color = in_color;\n"
                        + " var_normal = u_m * in_normal;\n"
                        + "}\n");
        glCompileShader(vertexShader);
        shaderErrorCheck(vertexShader,"VertexShader");
        glShaderSource(fragmentShader,
                "#version 330\n"
                        + "in vec4 var_color;\n"
                        + "in vec4 var_normal;\n"
                        + "out vec4 out_color;\n"
                        + "uniform vec3 u_light;\n"
                        + "void main(){\n"
                        + "	vec3 N = gl_FrontFacing ? normalize(var_normal.xyz) : normalize(-var_normal.xyz);\n"
                        + " vec3 L = normalize(u_light - gl_FragCoord.xyz);\n"
                        + " float ambient = 0.5f;\n"
                        + " float diffuse = max(dot(L, N), 0.0f);\n"
                        + "	out_color = var_color * (ambient + diffuse);\n"
                        + "}\n");
        glCompileShader(fragmentShader);
        shaderErrorCheck(fragmentShader,"FragmentShader");
        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
//        GLUtil.checkGLError();
        glBindAttribLocation(program, 1, "in_pos");
        glBindAttribLocation(program, 2, "in_color");
        glBindAttribLocation(program, 3, "in_normal");
        glBindFragDataLocation(program, 0, "out_color");
        glLinkProgram(program);
        matLoc = glGetUniformLocation(program, "u_m");
        lightLoc = glGetUniformLocation(program, "u_light");
//        GLUtil.checkGLError();
        glUseProgram(program);
        glUniform3f(lightLoc, 1.0f, -10.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
//        GLUtil.checkGLError();
    }

    private void shaderErrorCheck(int vertexShader, String name) {
        String log = glGetShaderInfoLog(vertexShader);
        if(log.length()>0) System.err.println("Build log "+name+": \n"+log);
        if(glGetShaderi(vertexShader, GL_COMPILE_STATUS)==GL_FALSE){
            glDeleteShader(vertexShader);
            throw new RuntimeException("Shader compilation error:\n"+log);
        }
    }

    private void initWindow() {
        System.out.println("LWJGL version: " + Version.getVersion());
        //glfwSetErrorCallback(errorCallbackPrint());

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        //glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_ANY_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        window = glfwCreateWindow(800, 600, "Demo", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        glfwShowWindow(window);
    }
}