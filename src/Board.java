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

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showInGameMenu();
                }
            }
        });

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

    private void showInGameMenu() {
        JDialog inGameMenu = new JDialog(this, "Pause Menu", true);
        inGameMenu.setSize(300, 200);
        inGameMenu.setLayout(new GridLayout(3, 1));

        // Resume button
        JButton resumeButton = new JButton("Resume");
        resumeButton.setFont(new Font("Arial", Font.PLAIN, 20));
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inGameMenu.dispose();  // Close the menu and resume the game
            }
        });

        // Restart button
        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 20));
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetBoard();  // Reset the game board
                inGameMenu.dispose();  // Close the menu
            }
        });

        // Quit button
        JButton quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.PLAIN, 20));
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);  // Exit the game
            }
        });

        inGameMenu.add(resumeButton);
        inGameMenu.add(restartButton);
        inGameMenu.add(quitButton);

        inGameMenu.setLocationRelativeTo(this);  // Center the menu on the screen
        inGameMenu.setVisible(true);
    }

    private void resetBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 0) {
                    board[row][col].setBackground(Color.WHITE);
                } else {
                    board[row][col].setBackground(Color.BLACK);
                    board[row][col].setText("");  // Clear any pieces
                }
            }
        }
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
                    System.out.println("color - " + selectedButton.getColor());
                    for (int[][] predictedMove : predicts.values()) {
                        for(int[] inner : predictedMove) {
                            int move_row = inner[0];
                            int move_col = inner[1];
                            System.out.println(move_row + " " + move_col + " ");
                            board[move_row][move_col].setBackground(Color.yellow);
                        }
                    }
                }
            }else{
                HashMap<String, int[][]> predicts= predictedMoves(selectedButton.getRow(), selectedButton.getColumn());
                if (predicts.get("regular_moves").length != 0 || predicts.get("captures").length != 0){
                    movePiece(clicked, predicts);
                    for (int[][] predictedMove : predicts.values()) {
                        for (int[] inner : predictedMove){
                            int move_row = inner[0];
                            int move_col = inner[1];
                            board[move_row][move_col].setBackground(Color.lightGray);
                        }
                    }
                }else {
                    selectedButton.setBackground(Color.lightGray);
                    selectedButton = null;
                }
            }
        }

        private void movePiece(Cell destButton, HashMap<String, int[][]> predicts) {
            if (destButton != selectedButton) {
                int[][] tempRegs = predicts.get("regular_moves");
                for(int i = 0; i < tempRegs.length; i++){
                    if (destButton.getRow() == tempRegs[i][0] && destButton.getColumn() == tempRegs[i][1]){
                        char selectedColor = selectedButton.getColor();
                        if (selectedColor == 'W' || selectedColor == 'Q') {
                            destButton.setButtonWhitePiece();
                            selectedButton.removeIcon();
                        } else if (selectedColor == 'B' || selectedColor == 'K') {
                            destButton.setButtonBlackPiece();
                            selectedButton.removeIcon();
                        }
                    }
                }
                int[][] tempCaps = predicts.get("captures");
                if(tempCaps.length != 0) {
                    for (int i = 0; i < tempCaps.length; i++) {
                        int capturedRow = (selectedButton.getRow() + destButton.getRow()) / 2;
                        int capturedCol = (selectedButton.getColumn() + destButton.getColumn()) / 2;

                        // Remove the captured piece from the board
                        board[capturedRow][capturedCol].removeIcon();
                        board[capturedRow][capturedCol].setBackground(Color.lightGray);
                        removeCaptured(capturedRow, capturedCol);

                        char selectedColor = selectedButton.getColor();
                        if (selectedColor == 'W' || selectedColor == 'Q') {
                            destButton.setButtonWhitePiece();
                            selectedButton.removeIcon();
                        } else if (selectedColor == 'B' || selectedColor == 'K') {
                            destButton.setButtonBlackPiece();
                            selectedButton.removeIcon();
                        }
                    }
                }

                move(selectedButton.getRow(), selectedButton.getColumn(), destButton.getRow(), destButton.getColumn());
            }
        }

        private native boolean isValidPieceToSelect(int row, int col);
        private native boolean isValidMove(int destRow, int destCol);
        private static native char[][] placingPieces();
        private native void move(int from_row, int from_col, int to_row, int to_col);
        private native void removeCaptured(int row, int col);
        private native HashMap<String, int[][]> predictedMoves(int row, int col);

    }
}
