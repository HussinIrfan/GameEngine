package SlRenderer;
import java.io.BufferedReader;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;

import javax.swing.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static csc133.spot.*;

import java.io.FileReader;
import java.io.IOException;
public class slSingleBatchRenderer {
    private static long glfw_window = 0;
    private static final int OGL_MATRIX_SIZE = 16;

    // don't call glCreateProgram() here - we have no gl-context here
    private static int shader_program;
    private static Matrix4f viewProjMatrix = new Matrix4f();
    private static FloatBuffer myFloatBuffer = BufferUtils.createFloatBuffer(OGL_MATRIX_SIZE);
    private static int vpMatLocation = 0, renderColorLocation = 0;
    private static slGoLBoardLive my_board;
    private static boolean[][] boardArray;
    private static boolean delayOn = false;
    private static boolean keepRunning = true;
    private static boolean toggleFR = false;
    private static boolean exitProgram = false;
    public static void render() {
        glfw_window = slWindow.get_oglwindow(WIN_WIDTH, WIN_HEIGHT);
        glfwSetKeyCallback(glfw_window, slKeyListener::keyCallback);


        try {
            renderLoop();
            slWindow.destroy_oglwindow();
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    } // void render()

    private static void renderLoop() {
        glfwPollEvents();
        initOpenGL();
        renderObjects();
        /* Process window messages in the main thread */
        while (!glfwWindowShouldClose(glfw_window)) {
            glfwWaitEvents();
        }
    } // void renderLoop()

    private static void initOpenGL() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glViewport(0, 0, WIN_WIDTH, WIN_HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        shader_program = glCreateProgram();
        int vs = glCreateShader(GL_VERTEX_SHADER);
        Matrix4f viewProjMatrix = new Matrix4f();
        glShaderSource(vs,
                "uniform mat4 viewProjMatrix;" +
                        "void main(void) {" +
                        " gl_Position = viewProjMatrix * gl_Vertex;" +
                        "}");
        glCompileShader(vs);
        glAttachShader(shader_program, vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs,
                "uniform vec3 renderColorLocation;" +
                        "void main(void) {" +
                        "gl_FragColor = vec4(renderColorLocation, 1.0);" +
                        "}");
        glCompileShader(fs);
        glAttachShader(shader_program, fs);
        glLinkProgram(shader_program);
        glUseProgram(shader_program);
        vpMatLocation = glGetUniformLocation(shader_program, "viewProjMatrix");
        return;
    } // void initOpenGL()

    private static float[] getVertexArray(int num_rows, int num_cols, int win_width, int win_height,
                                          float my_offset, float my_padding, float my_length) {
        int vps = 4; // Vertices Per Square
        int fpv = 2;  // Vertices Per Vertex
        float[] ret_array = new float[num_rows * num_cols * vps * fpv];
        float xmin = my_offset, xmax = xmin + my_length;
        float ymin = win_height - my_offset - my_length, ymax = win_height - my_offset;

        int indx = 0;
        for (int row = 0; row < num_rows; ++row) {
            for (int col = 0; col < num_cols; ++col) {
                ret_array[indx++] = xmin;
                ret_array[indx++] = ymin;

                ret_array[indx++] = xmax;
                ret_array[indx++] = ymin;

                ret_array[indx++] = xmax;
                ret_array[indx++] = ymax;

                ret_array[indx++] = xmin;
                ret_array[indx++] = ymax;

                xmin = xmax + my_padding;
                xmax = xmin + my_length;
            }  //  for (int col = 0; col < num_cols; ++col)
            xmin = my_offset;
            xmax = xmin + my_length;
            ymin -= (my_padding + my_length);
            ymax -= (my_padding + my_length);
        }  //  for (int row = 0; row < num_rows; ++row)
        return ret_array;
    }  // float[] getVertexArray(...)

    private static int[] getIndexArrayForSquares(int num_rows, int num_cols,
                                                 int indices_per_square, int verts_per_square) {
        int[] indx_array =
                new int[num_rows * num_cols * indices_per_square];
        int my_i = 0, v_indx = 0;
        while (my_i < indx_array.length) {
            indx_array[my_i++] = v_indx;
            indx_array[my_i++] = v_indx + 1;
            indx_array[my_i++] = v_indx + 2;

            indx_array[my_i++] = v_indx;
            indx_array[my_i++] = v_indx + 2;
            indx_array[my_i++] = v_indx + 3;

            v_indx += verts_per_square;
        }
        return indx_array;
    }  //  public int[] getIndexArrayForSquares(...)
    private static void userKeyInputs(){
        if(slKeyListener.isKeyPressed(GLFW_KEY_D))
        {
            delayOn = !delayOn;
            slKeyListener.resetKeypressEvent(GLFW_KEY_D);
            System.out.println("KeyPressed!");
        }
        if(slKeyListener.isKeyPressed(GLFW_KEY_H))
        {
            keepRunning = false;
            slKeyListener.resetKeypressEvent(GLFW_KEY_H);
            System.out.println("KeyPressed!");
        }
        if(slKeyListener.isKeyPressed(GLFW_KEY_SPACE))
        {
            keepRunning = true;
            slKeyListener.resetKeypressEvent(GLFW_KEY_SPACE);
            System.out.println("KeyPressed!");
        }
        if(slKeyListener.isKeyPressed(GLFW_KEY_F))
        {
            toggleFR = !toggleFR;
            slKeyListener.resetKeypressEvent(GLFW_KEY_F);
            System.out.println("KeyPressed!");
        }
        if(slKeyListener.isKeyPressed(GLFW_KEY_R))
        {
            my_board = new slGoLBoardLive(NUM_POLY_ROWS, NUM_POLY_COLS);
            boardArray = my_board.getLiveCellArray();
            slKeyListener.resetKeypressEvent(GLFW_KEY_R);
            System.out.println("KeyPressed!");
        }
        if(slKeyListener.isKeyPressed(GLFW_KEY_ESCAPE))
        {
            glfwSetWindowShouldClose(glfw_window, true);
            slKeyListener.resetKeypressEvent(GLFW_KEY_ESCAPE);
        }
        //JFileChooser fileChooser = new JFileChooser();
        if(slKeyListener.isKeyPressed(GLFW_KEY_L))
        {
            String myF = JOptionPane.showInputDialog("Enter FileName");

            try(BufferedReader reader = new BufferedReader(new FileReader(myF))){
                String line = reader.readLine();
                System.out.println(line);
                line = reader.readLine();
                System.out.println(line);

            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println(myF);
            slKeyListener.resetKeypressEvent(GLFW_KEY_L);

        }
    }

    private static void renderObjects() {
        int verts_per_square = 4;
        int indices_per_square = 6;
        int total_draw_verts = NUM_POLY_ROWS * NUM_POLY_COLS * verts_per_square;
        int total_draw_indices = NUM_POLY_ROWS * NUM_POLY_COLS * indices_per_square;

        float[] vertices = getVertexArray(NUM_POLY_ROWS, NUM_POLY_COLS, WIN_WIDTH, WIN_HEIGHT,
                POLY_OFFSET, POLY_PADDING, sq_length);
        int[] indices = getIndexArrayForSquares(NUM_POLY_ROWS, NUM_POLY_COLS,
                indices_per_square, verts_per_square);

        int vbo = glGenBuffers();
        int ibo = glGenBuffers();

        my_board = new slGoLBoardLive(NUM_POLY_ROWS, NUM_POLY_COLS);
        boardArray = my_board.getLiveCellArray(); //STORES A COPY OF MY_BOARD

        long start_time = System.currentTimeMillis();
        int numSquaresIndex = 0;
        float[][] squareColors = new float[NUM_POLY_ROWS * NUM_POLY_COLS][3]; //STORES RGB COLORS FOR EACH CELL


        while (!glfwWindowShouldClose(glfw_window)) {

            //Get framerate:
            long cur_time = System.currentTimeMillis();
            double delta_time = (cur_time - start_time);
            double framerate = 1 / (delta_time / 1000);
            start_time = cur_time;
            if(toggleFR)
                System.out.println("FrameRate:" + (int)framerate);


            glfwPollEvents();
            userKeyInputs(); //handle all hotkeys

            if(keepRunning) {
                //Set color for each square based on GoLBoard:
                for (int i = 0; i < NUM_POLY_ROWS; i++) {
                    for (int j = 0; j < NUM_POLY_COLS; j++) {
                        if (boardArray[i][j]) {
                            squareColors[numSquaresIndex][0] = 0.0f; // Red
                            squareColors[numSquaresIndex][1] = 1.0f; // Green
                            squareColors[numSquaresIndex][2] = 0.0f; // Blue
                        } else {
                            squareColors[numSquaresIndex][0] = 1.0f; // Red
                            squareColors[numSquaresIndex][1] = 0.0f; // Green
                            squareColors[numSquaresIndex][2] = 0.0f; // Blue
                        }
                        numSquaresIndex++;
                    }
                }
                //Reset index each frame and update board
                numSquaresIndex = 0;
                my_board.updateNextCellArray();
                boardArray = my_board.getLiveCellArray();


                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferData(GL_ARRAY_BUFFER, (FloatBuffer) BufferUtils.
                        createFloatBuffer(vertices.length).
                        put(vertices).flip(), GL_STATIC_DRAW);

                glEnableClientState(GL_VERTEX_ARRAY);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, (IntBuffer) BufferUtils.
                        createIntBuffer(indices.length).
                        put(indices).flip(), GL_STATIC_DRAW);

                int coords_per_vertex = 2, vertex_stride = 0;
                long first_vertex_ptr = 0L;
                glVertexPointer(coords_per_vertex, GL_FLOAT,
                        vertex_stride, first_vertex_ptr);

                slCamera my_cam = new slCamera();
                my_cam.setProjectionOrtho();
                viewProjMatrix = my_cam.getProjectionMatrix();

                glUniformMatrix4fv(vpMatLocation, false,
                        viewProjMatrix.get(myFloatBuffer));
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                // Loop sets color for each square and draws it:
                int indexSquare = 0;
                for (int i = 0; i < total_draw_indices; i += indices_per_square) {
                    glUniform3f(renderColorLocation, squareColors[indexSquare][0], squareColors[indexSquare][1], squareColors[indexSquare][2]);
                    indexSquare++;

                    long first_index_ptr = i * 4;
                    glDrawElements(GL_TRIANGLES, indices_per_square, GL_UNSIGNED_INT, first_index_ptr);
                }

                glfwSwapBuffers(glfw_window);
            }
            if (delayOn) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }//End While Loop
    }//RenderObjects()
}

