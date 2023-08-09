import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/*

The main goal for this program was to create an Artificial Intelligence for a game of Connect 4. The AI must run on minimax, an algorithm
for determining optimal plays in a turn based game. My minimax function follows pseudocode that was covered in lecture and tutorial.
My heuristic is a combination of a few things. If the AI can win, win the game. If the human player has a chance to win the game then set
the value to the lowest possible. If the extreme cases aren't found then calculate a value of how 'winning' the board is for the AI.
The Heuristic does this by first calculating how many 2 sequential pieces it can find. It then calculates how many 2's sequentially the
opponent has. It does the same thing with 3 sequential pieces. It then adds points for 3 sequential pieces and 2 sequential pieces and reduces points
for the opponents sequential pieces. If the value of the board is low the AI priorities the middle of the board. This helps the AI
choose a move at the beginning of the game.
*/

public class Connect4 {
    String[][] board = new String[6][7];
    boolean player = false;
    private static Scanner s = new Scanner(System.in);


    public Connect4() {
        fillBoard();
        printBoard();
        play();
    }

    //This method structures the program. Provides prompts for the user's columns and notifies the user when the column is full.

    public void play() {
        int depth;
        System.out.println("How deep do you want the AI to search (how deep to ply): ");
        depth = s.nextInt();

        while (true) {

            System.out.print("Enter a column: ");
            int play = s.nextInt();
            if (play < 0 | play > 6) {
                System.out.println("Column choices are between 0-6");
                play();
            }
            if (filledColumnCheck(play)) {
                System.out.println("This column is full.");
                play();
            }
            placePiece(play, player);
            printBoard();

            //if that play didn't win switch turns
            if (checkForWin(player, board)) {
                break;
            } else {
                if (player) {
                    player = false;
                } else {
                    player = true;
                }
            }
            //get all possible next moves
            ArrayList<String[][]> possibleStates = new ArrayList<>();
            possibleStates = getPossibleStates(board, player);

            //for all possible next moves take max score from minimax
            //since we pass the minimax algorithm all possible moves for the AI, the next set of moves would be the
            //humans moves therefore, the original call will be passed as false.
            int bestScore = Integer.MIN_VALUE;
            int minimaxScore, bestAIMove = 0;

            for (int i = 0; i < possibleStates.size(); i++) {
                minimaxScore = minimax(possibleStates.get(i), depth, false);
                if (minimaxScore > bestScore) {
                    bestScore = minimaxScore;
                    bestAIMove = i;
                }
            }
            placePiece(bestAIMove, player);
            printBoard();

            System.out.println("The AI has chosen column: " + bestAIMove);
            //if that play didn't win switch turns
            if (checkForWin(player, board)) {
                break;
            } else {
                if (player) {
                    player = false;
                } else {
                    player = true;
                }
            }

        }
        //if the while loop has broken then the game has been won
        if (player) {
            System.out.println("AI has won!");
        } else {
            System.out.println("You have won!");
        }
    }

    //This method follows the minimax algorithm covered in class and tutorial to find optimal moves.
    public int minimax(String[][] possibleState, int depth, boolean maximizingPlayer) {

        int value;
        ArrayList<String[][]> childStates;
        boolean won = false;
        if (possibleState == null)
            return Integer.MIN_VALUE;

        //if the depth is 0 or the node is a terminal node
        if (depth == 0 | checkForWin(maximizingPlayer, possibleState)) {
            //return the value the heuristic returns
            value = heuristic(possibleState, maximizingPlayer);
            return value;
        }

        if (maximizingPlayer) {
            value = Integer.MIN_VALUE;
            childStates = getPossibleStates(possibleState, maximizingPlayer);
            for (int i = 0; i < childStates.size(); i++) {
                if (childStates.get(i) != null) {
                    //find the max value of the nodes children
                    value = Math.max(value, minimax(childStates.get(i), depth - 1, false));
                }
            }
            return value;

        } else {
            value = Integer.MAX_VALUE;
            childStates = getPossibleStates(possibleState, maximizingPlayer);
            for (int i = 0; i < childStates.size(); i++) {
                //find the min value of the nodes children
                value = Math.min(value, minimax(childStates.get(i), depth - 1, true));
            }
            return value;
        }
    }

    //this method calculates the score of the board state for the AI. Higher the value the better the board state is for
    //the AI
    public int heuristic(String[][] possibleState, boolean maximizingPlayer) {
        int value;
        //if the board state is won for the human the board state is worst possible scenario
        if (checkForWin(player, possibleState)) {//if the AI can win set it to the highest possible value.
            value = Integer.MAX_VALUE;
            return value;
        } else if (checkForWin(!player, possibleState)) {//if the human has a chance to win set it to the lowest possible value
            value = Integer.MIN_VALUE;
            return value;
        }

        int countThree = 0, countTwo = 0, opponentThree = 0, opponentTwo = 0;

        //count the amount of two and three in a row each person has and adjust the value accordingly.
        countTwo = countInARow(player, possibleState, 2);
        countTwo *= 250;
        opponentTwo = countInARow(!player, possibleState, 2);
        opponentTwo *= -10;

        countThree = countInARow(player, possibleState, 3);
        countThree *= 1000;
        opponentThree = countInARow(!player, possibleState, 3);
        opponentThree *= -100;

        //if the value is very low prioritize the middle of the board,
        value = (countThree + countTwo + opponentThree + opponentTwo);
        if (value < 30 & value > -30) {
            value += ((countMiddle(player, possibleState)) * 10);

        }

        return value;
    }


    //this method counts how many middle tiles the AI could obtain and assigns a value accordingly. This method returns the value
    public int countMiddle(boolean player, String[][] board) {
        int count = 0;
        for (int i = 5; i >= 3; i--) {
            for (int j = 2; j < 5; j++) {
                if (board[i][j].equals("O")) {
                    if (i == 5 & j == 3) {
                        count += 50;
                    } else if (i == 5) {
                        count += 30;
                    } else if (i == 4) {
                        count += 5;
                    } else {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    //this method gets a board and a player and returns all possible moves for the player/.
    public ArrayList<String[][]> getPossibleStates(String[][] board, boolean player) {
        ArrayList<String[][]> possibleStates = new ArrayList<>();
        String[][] tmpBoard = new String[6][7];

        for (int i = 0; i < 7; i++) {
            tmpBoard = copyArray(board);
            if (!filledColumnCheck(i)) {
                possibleStates.add(placeTmpPiece(i, player, tmpBoard));
            } else {
                possibleStates.add(null);
            }
        }
        return possibleStates;
    }

    //Make a copy of the board for possibleStates
    public String[][] copyArray(String[][] board) {
        String[][] tmpBoard = new String[6][7];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                tmpBoard[i][j] = board[i][j];
            }
        }
        return tmpBoard;
    }

    //check for a win horizontally and return true if found
    public boolean checkForHorizontal(boolean player, String[][] board) {
        int count;
        boolean won = false;
        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 4; j++) {
                outerloop:
                {
                    count = 0;
                    for (int k = j; k < (j + 4); k++) {
                        if (player) {
                            if (board[i][k].equals("O")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        } else {
                            if (board[i][k].equals("X")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }
        return won;
    }

    //check for a win vertically and return true if found
    public boolean checkForVertical(boolean player, String[][] board) {
        int count;
        boolean won = false;
        for (int i = 5; i > 2; i--) {
            for (int j = 0; j <= board.length; j++) {
                outerloop:
                {
                    count = 0;
                    for (int k = i; k > (i - 4); k--) {
                        if (player) {
                            if (board[k][j].equals("O")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        } else {
                            if (board[k][j].equals("X")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }
        return won;
    }

    //sets up checkForDiangonalLeft and checkForDiangonalRight
    public boolean checkForDiagonal(boolean player, String[][] board) {
        if (checkForDiagonalRight(player, board) | checkForDiagonalLeft(player, board)) {
            return true;
        }
        return false;
    }

    //Check for a win in the diagonal going upward and to the right. and return true if found
    public boolean checkForDiagonalRight(boolean player, String[][] board) {
        int count;
        boolean won = false;
        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 4; j++) {
                outerloop:
                {
                    count = 0;
                    for (int k = j; k < (j + 4); k++) {
                        if (player) {
                            if ((i - count) >= 0 && board[i - count][k].equals("O")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        } else {
                            if ((i - count) >= 0 && board[i - count][k].equals("X")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }
        return won;
    }

    //check for a win in the diagonal going up and to the left. Returns true if found.
    public boolean checkForDiagonalLeft(boolean player, String[][] board) {
        int count;
        boolean won = false;
        for (int i = 5; i >= 0; i--) {
            for (int j = 6; j > 2; j--) {
                outerloop:
                {
                    count = 0;
                    for (int k = j; k > (j - 4); k--) {
                        if (player) {
                            if ((i - count) >= 0 && board[i - count][k].equals("O")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        } else {
                            if ((i - count) >= 0 && board[i - count][k].equals("X")) {
                                count++;
                                if (count == 4) {
                                    won = true;
                                    break;
                                }
                            } else {
                                break outerloop;
                            }
                        }
                    }
                }
            }
        }
        return won;
    }


    //count how many in a row a player has. Returns the count of how many times it finds inARow of the same player.
    public int countHorizontal(boolean player, String[][] board, int inARow) {
        int count, value = 0;
        for (int i = 5; i >= 0; i--) {
            outerloop:
            {
                for (int j = 0; j < 7; j++) { //if the check would go out of the board go to the next row instead
                    if ((j + inARow) > 6) {
                        break outerloop;
                    }
                    count = 0;
                    for (int k = j; k < (j + inARow); k++) {
                        if (player) {
                            if (board[i][k].equals("O")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                    j += inARow;//don't count the same one twice. For example three in a row as 2x two in a row
                                    break outerloop;
                                }
                            }
                        } else {
                            if (board[i][k].equals("X")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                    j += inARow;
                                    break outerloop;
                                }
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    //count how many in a vertically continuously a player has. Returns the count of how many times it finds inARow of the same player.
    public int countVertical(boolean player, String[][] board, int inARow) {
        int count, value = 0;
        for (int i = 5; i >= 0; i--) {
            if ((i - inARow) < 0) break;
            for (int j = 0; j < 7; j++) {

                count = 0;
                for (int k = i; k > (i - inARow); k--) {
                    if (player) {
                        if (board[k][j].equals("O")) {
                            count++;
                            if (count == inARow) {
                                value++;

                            }
                        }
                    } else {
                        if (board[k][j].equals("X")) {
                            count++;
                            if (count == inARow) {
                                value++;
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    //sets up countDiagonalRight and countDiagonalLeft.
    public int countDiagonal(boolean player, String[][] board, int inARow) {
        int value;
        value = countDiagonalRight(player, board, inARow) + countDiagonalLeft(player, board, inARow);

        return value;
    }

    //counts how many times a diagonal up and to right. Returns the count of how many times it finds inARow of the same player.
    public int countDiagonalRight(boolean player, String[][] board, int inARow) {
        int count, value = 0;
        boolean won = false;
        for (int i = 5; i >= 0; i--) {
            outerloop:
            {
                for (int j = 0; j < 7; j++) { //if the check would go out of the board go to the next row instead
                    if ((j + inARow) > 6) {
                        break outerloop;
                    }
                    count = 0;
                    for (int k = j; k < (j + inARow); k++) {
                        if (player) {
                            if ((i - count) >= 0 && board[i - count][k].equals("O")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                }
                            }
                        } else {
                            if ((i - count) >= 0 && board[i - count][k].equals("X")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return value;
    }

    ////counts how many times a diagonal up and to left. Returns the count of how many times it finds inARow of the same player.
    public int countDiagonalLeft(boolean player, String[][] board, int inARow) {
        int count, value = 0;
        boolean won = false;
        for (int i = 5; i >= 0; i--) {
            outerloop:
            {
                for (int j = 6; j > 0; j--) {
                    if ((j - inARow) < 0) {//if the check would go out of the board go to the next row instead
                        break outerloop;
                    }
                    count = 0;
                    for (int k = j; k > (j - inARow); k--) {
                        if (player) {
                            if ((i - count) >= 0 && board[i - count][k].equals("O")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                }
                            }
                        } else {
                            if ((i - count) >= 0 && board[i - count][k].equals("X")) {
                                count++;
                                if (count == inARow) {
                                    value++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return value;
    }


    //check for a win. If any of the called methods returns true then return true.
    public boolean checkForWin(boolean player, String[][] board) {
        boolean won;
        if (checkForHorizontal(player, board) | checkForVertical(player, board) | checkForDiagonal(player, board)) {
            return true;
        }
        return false;
    }

    //count how many continuously horizontally, vertically, and diagonally for inARow amount. return the count
    public int countInARow(boolean player, String[][] board, int inARow) {
        int value = 0;
        value = (countHorizontal(player, board, inARow) + countVertical(player, board, inARow) + countDiagonal(player, board, inARow));
        return value;
    }

    //this method checks if a column in the board is filled. returns true if the spot is filled and false if it's open.
    public boolean filledColumnCheck(int column) {
        if (!board[0][column].equals(" ")) {
            return true;
        }
        return false;
    }

    //This method places a piece on the board. it's receives column and the player placing the piece. counts up in the column until a open spot is found then places.
    public void placePiece(int column, boolean player) {
        for (int row = 5; row > -1; row--) {
            if (board[row][column].equals(" ")) {
                if (player) {
                    board[row][column] = "O";
                } else {
                    board[row][column] = "X";
                }
                System.out.println();
                break;
            }
        }
    }

    //places a temporary piece so that getPossibleStates can place a piece in the temporary board.
    public String[][] placeTmpPiece(int column, boolean player, String[][] board) {
        for (int row = 5; row > -1; row--) {
            if (board[row][column].equals(" ")) {
                if (player) {
                    board[row][column] = "O";
                } else {
                    board[row][column] = "X";
                }
                break;
            }
        }
        return board;
    }


    //print the board to console.
    public void printBoard() {
        for (int k = 0; k < board[0].length; k++) {
            System.out.print(" " + (k));
        }
        System.out.println();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                System.out.print("|");
                System.out.print(board[i][j]);
            }
            System.out.println("|");
        }
    }

    //initialize the board by placing spaces in the array.
    public void fillBoard() {
        for (String[] row : board) {
            Arrays.fill(row, " ");
        }
    }


    public static void main(String[] args) {
        Connect4 c = new Connect4();
    }
}