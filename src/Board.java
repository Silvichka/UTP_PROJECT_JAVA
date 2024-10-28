import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Board extends JFrame{

    private final int SIZE = 8;

    private Cell[][] board;
    private Cell selectedButton;

    public Board(){
        setTitle("Checkers");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(SIZE, SIZE));
        setResizable(false);

        board = new Cell[SIZE][SIZE];
        intializeBoard();
        setVisible(true);

        // Ensure the board has focus to listen to key events
        setFocusable(true);
    }

    public void intializeBoard(){

        char[][] tempBoard = CheckListener.placingPieces();

        for (int row = 0; row < SIZE; row++){
            for (int col = 0; col < SIZE; col++){
                Cell button = new Cell(row, col);
                button.setOpaque(true);
                button.setBorderPainted(false);

                if ((row + col) % 2 == 1){
                    button.setBackground(Color.lightGray);
                }else{
                    button.setBackground(Color.WHITE);
                }

                if(tempBoard[row][col] == 'W') {
                    button.setButtonWhitePiece();
                }else if(tempBoard[row][col] == 'B') {
                    button.setButtonBlackPiece();
                }

                button.addActionListener(new CheckListener(row, col));

                board[row][col] = button;
                add(button);
            }
        }
    }

    public void showWinnerDialog(int winner) {
        String winnerName = winner == 2 ? "White" : "Black";
        String message = winnerName + " wins!";

        // Create a dialog to display the winner and restart option
        int response = JOptionPane.showConfirmDialog(null, message, "Game Over", JOptionPane.OK_CANCEL_OPTION);

        // If the user clicks the "OK" button, restart the game
        if (response == JOptionPane.OK_OPTION) {
            restartGame();
        }
    }

    public void restartGame() {
        SwingUtilities.invokeLater(Board::new); // Reset the board
        System.out.println("Game restarted!");
        CheckListener.restart();// Placeholder for your game restart logic
    }

    private class CheckListener implements ActionListener{

        int row, col;

        public CheckListener(int row, int cols){
            this.row = row;
            this.col = cols;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Cell clicked = board[row][col];

            if (selectedButton == null){
                if(!isValidPieceToSelect(row, col)){
                    selectedButton = null;
                }else if(isValidPieceToSelect(row, col)) {
                    HashMap<String, int[][]> predicts = predictedMoves(row, col);
                    selectedButton = clicked;
//                    System.out.println("color - " + selectedButton.getColor());
                    highlightPredictedMoves(predicts);
                }
            }else{
                clearPredictedMoves();

                HashMap<String, int[][]> predicts= predictedMoves(selectedButton.getRow(), selectedButton.getColumn());
                int[][] captureMoves = predicts.get("captures");
//                System.out.println(Arrays.toString(captureRule(clicked.getRow(), clicked.getColumn())));
                int[] captRule = captureRule(clicked.getRow(), clicked.getColumn());

                if (isDestInMoves(clicked, captureMoves, predicts.get("regular_moves"))) {
                    // Player has chosen a valid move
                    if (captureMoves.length > 0 && !isDestInMoves(clicked, captureMoves, new int[0][])) {
                        // Penalize the player if a capture is possible but they selected a non-capture move
//                        System.out.println("Capture available but not chosen. Removing piece as penalty.");
//                        move(selectedButton.getRow(), selectedButton.getColumn(), selectedButton.getRow(), selectedButton.getColumn(), true);
                        selectedButton.removeIcon();
                        removeCaptured(selectedButton.getRow(), selectedButton.getColumn());
                    } else if(captRule.length != 0 && (selectedButton.getRow() != captRule[0] && selectedButton.getColumn() != captRule[1])){
                        board[captRule[0]][captRule[1]].removeIcon();
                        removeCaptured(captRule[0], captRule[1]);
                        movePiece(clicked, predicts);
                    }else {
                        movePiece(clicked, predicts);
                    }
                }
                selectedButton = null;
                if(winner() != 0){
                    showWinnerDialog(winner());
                }
            }
        }

        private void movePiece(Cell destButton, HashMap<String, int[][]> predicts) {
            int[][] tempRegs = predicts.get("regular_moves");
            int[][] tempCaps = predicts.get("captures");

            if (destButton != selectedButton && isDestInMoves(destButton, tempCaps, tempRegs)) {
                if (tempCaps.length != 0) {
                    // Capture move
                    int capturedRow = (selectedButton.getRow() + destButton.getRow()) / 2;
                    int capturedCol = (selectedButton.getColumn() + destButton.getColumn()) / 2;

                    // Remove the captured piece from the board
                    board[capturedRow][capturedCol].removeIcon();
                    removeCaptured(capturedRow, capturedCol);

                    // Move the selected piece to the new destination
                    char selectedColor = selectedButton.getColor();
                    if (selectedColor == 'W') {
                        destButton.setButtonWhitePiece();
                    } else if (selectedColor == 'B') {
                        destButton.setButtonBlackPiece();
                    }
                    selectedButton.removeIcon();

                    // Update the board with the move
                    System.out.println(selectedButton.getRow() + "" + selectedButton.getColumn() +""+ destButton.getRow() +""+ destButton.getColumn());
                    move(selectedButton.getRow(), selectedButton.getColumn(), destButton.getRow(), destButton.getColumn(), false);
                    selectedButton = destButton;  // Keep the selected button as the new destination

                    // Check for additional captures from the new position
                    HashMap<String, int[][]> additionalCaptures = predictedMoves(destButton.getRow(), destButton.getColumn());
                    if (additionalCaptures.get("captures").length > 0) {
                        highlightPredictedMoves(additionalCaptures);  // Highlight further captures
                        return;  // Hold the turn for additional captures
                    } else {
                        move(0,0,0,0, true);
                    }
                } else {
                    // Regular move (non-capturing)
                    for (int i = 0; i < tempRegs.length; i++) {
                        if (destButton.getRow() == tempRegs[i][0] && destButton.getColumn() == tempRegs[i][1]) {
                            char selectedColor = selectedButton.getColor();
                            if (selectedColor == 'W') {
                                destButton.setButtonWhitePiece();
                            } else if (selectedColor == 'B') {
                                destButton.setButtonBlackPiece();
                            }
                            selectedButton.removeIcon();
                        }
                    }

                    // Update the board with the move
                    move(selectedButton.getRow(), selectedButton.getColumn(), destButton.getRow(), destButton.getColumn(), true);
                }
            }
            selectedButton = null;  // Reset the selected button if no additional captures
            clearPredictedMoves();   // Clear highlighted moves

            // Check for winner after the turn is completed
            if (winner() != 0) {
                showWinnerDialog(winner());
            }

            display();
        }


        private boolean isDestInMoves(Cell dest, int[][] caps, int[][] regs){
            for(int[] x : caps){
                if(dest.getRow() == x[0] && dest.getColumn() == x[1])return true;
            }
            for(int[] x : regs){
                if(dest.getRow() == x[0] && dest.getColumn() == x[1])return true;
            }
            return false;
        }

        private void clearPredictedMoves() {
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    if ((row + col) % 2 == 1) {
                        board[row][col].setBackground(Color.lightGray);
                    }
                }
            }
        }

        private void highlightPredictedMoves(HashMap<String, int[][]> predicts) {
            for (int[][] predictedMove : predicts.values()) {
                for (int[] inner : predictedMove) {
                    int move_row = inner[0];
                    int move_col = inner[1];
                    board[move_row][move_col].setBackground(Color.yellow);
                }
            }
        }

        private void display(){
            char[][] c = displaying();

            for(char[] x : c){
                for(char y : x){
                    System.out.print(y);
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
        }

        private int winner(){
            int white = 0;
            int black = 0;

            for (Cell[] x : board){
                for(Cell c : x){
                    if(c.getColor() == 'W')white++;
                    if(c.getColor() == 'B')black++;
                }
            }

            if(black == 0){
                return 2;//if white wins
            }else if(white == 0){
                return 1;//if black wins
            }
            return 0;
        }

        private native boolean isValidPieceToSelect(int row, int col);
        private static native char[][] placingPieces();
        private native void move(int from_row, int from_col, int to_row, int to_col, boolean changeTurn);
        private native void removeCaptured(int row, int col);
        private native HashMap<String, int[][]> predictedMoves(int row, int col);
        private native int[] captureRule(int rowDest, int colDest);
        private static native void restart();

        private native char[][] displaying();

    }
}
