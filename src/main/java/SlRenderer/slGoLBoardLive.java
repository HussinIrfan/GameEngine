package SlRenderer;
public class slGoLBoardLive extends slGoLBoard{
    public slGoLBoardLive(int numRows, int numCols) {
        super(numRows, numCols);
    }
    public slGoLBoardLive(int numRows, int numCols, int numAlive) {
        super(numRows, numCols, numAlive);
    }

    @Override public int countLiveTwoDegreeNeighbors(int row, int col){
        int counter = 0;
        int prev_r = (row-1 + NUM_ROWS) % NUM_ROWS;
        int next_r = (row + 1) % NUM_ROWS;
        int prev_c = (col - 1 + NUM_COLS) % NUM_COLS;
        int next_c = (col + 1) % NUM_COLS;

        counter += (liveCellArray[prev_r][prev_c]) ? 1 : 0;
        counter += (liveCellArray[row][prev_c]) ? 1 : 0;
        counter += (liveCellArray[next_r][prev_c]) ? 1 : 0;
        counter += (liveCellArray[next_r][col]) ? 1 : 0;
        counter += (liveCellArray[next_r][next_c]) ? 1 : 0;
        counter += (liveCellArray[row][next_c]) ? 1 : 0;
        counter += (liveCellArray[prev_r][next_c]) ? 1 : 0;
        counter += (liveCellArray[prev_r][col]) ? 1 : 0;
        return counter;

    }


    // return how many live cells are in the updated board
    /*
        Rules:
        1. Live Two Degree Neighbors < 2 --> Kill
        2. Live Two Degree Neighbors == 2 || Live Neighbors == 3 --> Retain
        3. Live Two Degree Neighbors > 3 --> Kill
        4. Dead with Live Two Degree Neighbors == 3 --> Alive again
     */

    //This function will call the function I make. It will use my function
    //to determine how many adjacent cells were alive, using that counter
    //it determines what to set each cell to in the updatedArray, return num.
    @Override public int updateNextCellArray() {
        int retVal = 0;

        int nln = 0;  // Number Live Neighbors
        boolean ccs = true; // Current Cell Status
        for (int row = 0; row < NUM_ROWS; ++row){
            for (int col = 0; col < NUM_COLS; ++col) {
                ccs = liveCellArray[row][col];
                nln = countLiveTwoDegreeNeighbors(row, col);
                if (ccs) {  //Rule 4
                    if(nln < 2 || nln > 3){
                        nextCellArray[row][col] = false;
                    }
                    else if(nln == 2 || nln == 3){
                        nextCellArray[row][col] = true;
                    }
                    ++retVal;
                }
                else{
                    if(nln != 3){
                        nextCellArray[row][col] = false;
                    }
                    else
                        nextCellArray[row][col] = true;
                    ++retVal;
                }

            }  // for (int row = 0; ...)
        }  //  for (int col = 0; ...)

        boolean[][] tmp = liveCellArray;
        liveCellArray = nextCellArray;
        nextCellArray = tmp;

        return retVal;
    }  //  int updateNextCellArray()
}

