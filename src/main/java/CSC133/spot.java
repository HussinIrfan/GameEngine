package csc133;

public class spot {
    public static final float POLY_OFFSET = 20.0f, POLY_PADDING = 10.0f, sq_length = 30;
    public static final int NUM_POLY_ROWS = 20, NUM_POLY_COLS = 18;
    public static int WIN_WIDTH =(int)(2 * POLY_OFFSET + (NUM_POLY_COLS-1) * POLY_PADDING + NUM_POLY_COLS * sq_length),
                      WIN_HEIGHT = (int)(2 * POLY_OFFSET + (NUM_POLY_ROWS-1) * POLY_PADDING + NUM_POLY_ROWS * sq_length);
}
