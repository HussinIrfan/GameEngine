package SlRenderer;
import java.io.*;

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
    private static boolean[] boardMapped = new boolean[NUM_POLY_ROWS * NUM_POLY_COLS]; //1-D copy of boardArray, used to loop through color
    private static boolean toggleDelay = false;
    private static boolean keepRunning = true;
    private static boolean toggleFR = false;
    private static boolean toggleLoad = false;

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
            toggleDelay = !toggleDelay;
            slKeyListener.resetKeypressEvent(GLFW_KEY_D);
            System.out.println("KeyPressed!");
        } //D Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_H))
        {
            keepRunning = false;
            slKeyListener.resetKeypressEvent(GLFW_KEY_H);
            System.out.println("KeyPressed!");
        } //H Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_SPACE))
        {
            keepRunning = true;
            slKeyListener.resetKeypressEvent(GLFW_KEY_SPACE);
            System.out.println("KeyPressed!");
        } //SPACE Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_F))
        {
            toggleFR = !toggleFR;
            slKeyListener.resetKeypressEvent(GLFW_KEY_F);
            System.out.println("KeyPressed!");
        } //F Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_R))
        {
            my_board = new slGoLBoardLive(NUM_POLY_ROWS, NUM_POLY_COLS);
            boardArray = my_board.getLiveCellArray();
            slKeyListener.resetKeypressEvent(GLFW_KEY_R);
            System.out.println("KeyPressed!");
        }//R Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_ESCAPE))
        {
            glfwSetWindowShouldClose(glfw_window, true);
            slKeyListener.resetKeypressEvent(GLFW_KEY_ESCAPE);
        } //ESC Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_L))
        {
            toggleLoad = true; //Ensures board is loaded engine is paused so user can see loaded board
            System.out.println("Board Loaded! Press SPACE to resume");
            String myF = JOptionPane.showInputDialog("Enter FileName"); //Open Box
            if(myF != null) {
                String directory = System.getProperty("user.dir"); //Get Directory for Files
                String path = directory + "/" + myF; //Combine Directory and UserInput into one String

                try (BufferedReader reader = new BufferedReader(new FileReader(path))) { //Create BufferedReader for loaded file
                    my_board.loadFileCellArray(reader); //Reads the loaded file and updates liveCellArray to match loaded file
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            slKeyListener.resetKeypressEvent(GLFW_KEY_L);
        } //L Key
        if(slKeyListener.isKeyPressed(GLFW_KEY_S))
        {
            keepRunning = false; //stop engine so user can see the board they saved

            String mySaveFile = JOptionPane.showInputDialog("Enter name for save file"); //Open Box
            if(mySaveFile != null && !mySaveFile.isEmpty()) {
                if (!mySaveFile.endsWith(".ca")) { //Ensure .ca is at end of User Inputted File Name
                    mySaveFile = mySaveFile + ".ca";
                }
                System.out.println("Board Saved! Press SPACE to resume");
                StringBuilder my_string = new StringBuilder(); //Create Mutable String

                my_string.append(NUM_POLY_ROWS + "\n"); //First Row is Num_Rows
                my_string.append(NUM_POLY_COLS + "\n"); //Second Row is Num_Cols
                int k = 0;
                for(int i = 0; i < NUM_POLY_COLS*NUM_POLY_ROWS; i++) //Loop through boardMapped and append string with either 1 or 0
                {
                    if(boardMapped[i])
                        my_string.append(1);
                    else
                        my_string.append(0);
                    my_string.append(" ");//Add whitespace after each char
                    k++;
                    if(k==NUM_POLY_COLS)
                    {
                        my_string.append("\n"); //NewLine after each row is finished
                        k=0;
                    }

                }
                //for (int i = 0; i < NUM_POLY_ROWS; i++) //Loop through boardArray and append string with either 1 or 0
                //{
                    //for (int j = 0; j < NUM_POLY_COLS; j++) {
                        //if (boardArray[i][j])
                            //my_string.append(1);
                        //else
                            //my_string.append(0);
                        //my_string.append(" "); //Add whitespace after each char
                    //}
                    //my_string.append("\n"); //NewLine after each row is finished
                //}
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(mySaveFile))) { //take my_string and write it to the file
                    writer.write(my_string.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("File Name Required! Press SPACE to resume or Press S to try again");
            }
            slKeyListener.resetKeypressEvent(GLFW_KEY_S);
        } //S Key
        if((slKeyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || slKeyListener.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) && slKeyListener.isKeyPressed(GLFW_KEY_SLASH))
        {
            System.out.println("Print this text --> ?" +
                    "\nToggle 500 ms frame delay --> d" +
                    "\nToggle frame rate display --> f" +
                    "\nHalt the engine --> h" +
                    "\nResume  engine --> SPACE" +
                    "\nSave engine state to a file --> s" +
                    "\nLoad engine state from a file --> l" +
                    "\nrandomize arrays and restart engine --> r" +
                    "\nExit Program --> ESC");
            slKeyListener.resetKeypressEvent(GLFW_KEY_SLASH);
            slKeyListener.resetKeypressEvent(GLFW_KEY_LEFT_SHIFT);
            slKeyListener.resetKeypressEvent(GLFW_KEY_RIGHT_SHIFT);
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

        my_board = new slGoLBoardLive(NUM_POLY_ROWS, NUM_POLY_COLS); //Create New Board
        boardArray = my_board.getLiveCellArray(); //2-D Array Points to the liveCellArray inside my_board
        //boolean[] boardMapped = new boolean[NUM_POLY_ROWS * NUM_POLY_COLS]; //1-D copy of boardArray, used to loop through color

        long start_time = System.currentTimeMillis();

        while (!glfwWindowShouldClose(glfw_window)) {
            //Get framerate:
            long cur_time = System.currentTimeMillis();
            double delta_time = (cur_time - start_time);
            double framerate = 1 / (delta_time / 1000);
            start_time = cur_time;

            //Toggle FrameRate Display:
            if(toggleFR)
                System.out.println("FrameRate:" + (int)framerate);


            glfwPollEvents();
            userKeyInputs(); //Function handles all hotkeys

            //Toggle Halt Key:
            if(keepRunning) {

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

                //Map 2-D boardArray to 1-D Array:
                int k = 0;
                for (int i = 0; i < NUM_POLY_ROWS; i++) {
                    for (int j = 0; j < NUM_POLY_COLS; j++) {
                        if (boardArray[i][j]) {
                            boardMapped[k] = true;
                        } else {
                            boardMapped[k] = false;
                        }
                        k++;
                    }
                }

                long ibps = 24;
                int dvps = 6;
                //Draw Each Square Either Red Or Green:
                for (int ci = 0; ci < NUM_POLY_ROWS * NUM_POLY_COLS; ++ci) {
                    if (boardMapped[ci]) {
                        glUniform3f(renderColorLocation, liveColor.x, liveColor.y, liveColor.z);
                    } else {
                        glUniform3f(renderColorLocation, deadColor.x, deadColor.y, deadColor.z);
                    }
                    glDrawElements(GL_TRIANGLES, dvps, GL_UNSIGNED_INT, ibps*ci);
                }  //  for (int ci = 0; ci < NUM_POLY_ROWS * NUM_POLY_COLS; ++ci)

                //Update my_board, will also update boardArray:
                my_board.updateNextCellArray();
                boardArray = my_board.getLiveCellArray();
                if(toggleLoad)
                {
                    keepRunning = false;
                    toggleLoad = false;
                }
                glfwSwapBuffers(glfw_window);
            }
            //Toggle Delay Key:
            if (toggleDelay) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }//End While Loop
    }//RenderObjects()
}

