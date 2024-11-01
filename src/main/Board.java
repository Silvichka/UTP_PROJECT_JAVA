package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class Board extends JFrame{

    private final int SIZE = 8;

    private Cell[][] board;
    private Cell selectedButton;
    private boolean isKeyboardMode;

    public Board() {
        setTitle("Checkers");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(SIZE, SIZE));
        setResizable(false);

        board = new Cell[SIZE][SIZE];
        intializeBoard();
        setVisible(true);
        setFocusable(true);
        initializeMenuBar();

        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocus();
        initializeKeyboardControls();
    }

    private void initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem restartItem = new JMenuItem("Restart");

        restartItem.addActionListener(e -> restartGame());

        gameMenu.add(restartItem);
        menuBar.add(gameMenu);

        setJMenuBar(menuBar);
    }

    private void initializeKeyboardControls() {

        addKeyListener(new KeyAdapter() {
            private int selectedRow = 0;
            private int selectedCol = 0;

            @Override
            public void keyPressed(KeyEvent e) {
                if (!isKeyboardMode) {
                    switchToKeyboardMode();
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> moveSelection(-1, 0);
                    case KeyEvent.VK_DOWN -> moveSelection(1, 0);
                    case KeyEvent.VK_LEFT -> moveSelection(0, -1);
                    case KeyEvent.VK_RIGHT -> moveSelection(0, 1);
                    case KeyEvent.VK_ENTER -> handleSelection();
                }
            }

            private void moveSelection(int rowOffset, int colOffset) {
                int newRow = selectedRow + rowOffset;
                int newCol = selectedCol + colOffset;

                if (newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE) {
                    resetHighlight(selectedRow, selectedCol);
                    selectedRow = newRow;
                    selectedCol = newCol;
                    highlightCell(selectedRow, selectedCol);
                }
            }

            private void handleSelection() {
                Cell clicked = board[selectedRow][selectedCol];
                if (selectedButton == null){
                    if(!isValidPieceToSelect(selectedRow, selectedCol)){
                        selectedButton = null;
                    }else if(isValidPieceToSelect(selectedRow, selectedCol)) {
                        HashMap<String, int[][]> predicts = predictedMoves(selectedRow, selectedCol);
                        selectedButton = clicked;
                        board[selectedButton.getRow()][selectedButton.getColumn()].setBorderPainted(true);
        //                    System.out.println("color - " + selectedButton.getColor());
                        highlightPredictedMoves(predicts);
                    }
                }else{
                    checkingCapturesRules(clicked);

                    selectedButton = null;
                    if(winner() != 0){
                        showWinnerDialog(winner());
                    }
                }
            }
        });
    }

    private void switchToKeyboardMode() {
        isKeyboardMode = true;
        clearPredictedMoves();
        selectedButton = null;
        requestFocus();
    }

    private void switchToMouseMode() {
        isKeyboardMode = false;
        clearPredictedMoves();
        selectedButton = null;
        requestFocus();
    }

    private void highlightCell(int row, int col) {
        board[row][col].setBackground(Color.orange);
    }

    private void resetHighlight(int row, int col) {
        if ((row + col) % 2 == 1) {
            board[row][col].setBackground(Color.lightGray);
        } else {
            board[row][col].setBackground(Color.WHITE);
        }
    }

    public void intializeBoard(){

        char[][] tempBoard = placingPieces();

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

                button.setFocusable(false);
                button.addActionListener(new CheckListener(row, col));

                board[row][col] = button;
                add(button);
            }
        }
    }

    public void showWinnerDialog(int winner) {
        String winnerName = winner == 2 ? "White" : "Black";
        String message = winnerName + " wins!";

        int response = JOptionPane.showConfirmDialog(null, message, "Game Over", JOptionPane.OK_CANCEL_OPTION);

        if (response == JOptionPane.OK_OPTION) {
            restartGame();
        }
    }

    public void restartGame() {
        SwingUtilities.invokeLater(Board::new);
        System.out.println("Game restarted!");
        restart();
    }

    public void movePiece(Cell destButton, HashMap<String, int[][]> predicts) {
        int[][] tempRegs = predicts.get("regular_moves");
        int[][] tempCaps = predicts.get("captures");

        if (destButton != selectedButton && isDestInMoves(destButton, tempCaps, tempRegs)) {
            if (tempCaps.length != 0) {
                int capturedRow = (selectedButton.getRow() + destButton.getRow()) / 2;
                int capturedCol = (selectedButton.getColumn() + destButton.getColumn()) / 2;

                board[capturedRow][capturedCol].removeIcon();
                removeCaptured(capturedRow, capturedCol);

                char selectedColor = selectedButton.getColor();
                if (selectedColor == 'W') {
                    destButton.setButtonWhitePiece();
                } else if (selectedColor == 'B') {
                    destButton.setButtonBlackPiece();
                }
                selectedButton.removeIcon();

//                System.out.println(selectedButton.getRow() + "" + selectedButton.getColumn() +""+ destButton.getRow() +""+ destButton.getColumn());
                move(selectedButton.getRow(), selectedButton.getColumn(), destButton.getRow(), destButton.getColumn(), false);
                selectedButton = destButton;

                HashMap<String, int[][]> additionalCaptures = predictedMoves(destButton.getRow(), destButton.getColumn());
                if (additionalCaptures.get("captures").length > 0) {
                    highlightPredictedMoves(additionalCaptures);
                    return;
                } else {
                    move(0,0,0,0, true);
                }
            } else if(!isBackwardMove(destButton)){
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

                move(selectedButton.getRow(), selectedButton.getColumn(), destButton.getRow(), destButton.getColumn(), true);
            }
        }
        selectedButton = null;
        clearPredictedMoves();

        if (winner() != 0) {
            showWinnerDialog(winner());
        }

//        displayJava();
    }

    public boolean isDestInMoves(Cell dest, int[][] caps, int[][] regs){
        for(int[] x : caps){
            if(dest.getRow() == x[0] && dest.getColumn() == x[1])return true;
        }
        for(int[] x : regs){
            if(dest.getRow() == x[0] && dest.getColumn() == x[1])return true;
        }
        return false;
    }

    public void clearPredictedMoves() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col].setBackground(Color.lightGray);
                    board[row][col].setBorderPainted(false);
                }
            }
        }
    }

    public void highlightPredictedMoves(HashMap<String, int[][]> predicts) {
        for (int[][] predictedMove : predicts.values()) {
            for (int[] inner : predictedMove) {
                int move_row = inner[0];
                int move_col = inner[1];
                board[move_row][move_col].setBackground(Color.yellow);
            }
        }
    }

    public int winner(){
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

    public boolean isBackwardMove(Cell destButton) {
        if (selectedButton.getColor() == 'W') {
            return destButton.getRow() > selectedButton.getRow();
        } else if (selectedButton.getColor() == 'B') {
            return destButton.getRow() < selectedButton.getRow();
        }
        return false;
    }

    public void checkingCapturesRules(Cell clicked){
        HashMap<String, int[][]> predicts= predictedMoves(selectedButton.getRow(), selectedButton.getColumn());
        int[][] captureMoves = predicts.get("captures");
//                System.out.println(Arrays.toString(captureRule(clicked.getRow(), clicked.getColumn())));
        int[] captRule = captureRule(clicked.getRow(), clicked.getColumn());

        if (isDestInMoves(clicked, captureMoves, predicts.get("regular_moves"))) {
            if (captureMoves.length > 0 && !isDestInMoves(clicked, captureMoves, new int[0][])) {
//                        System.out.println("Capture available but not chosen. Removing piece as penalty.");
                move(selectedButton.getRow(), selectedButton.getColumn(), selectedButton.getRow(), selectedButton.getColumn(), true);
                selectedButton.removeIcon();
                removeCaptured(selectedButton.getRow(), selectedButton.getColumn());
                clearPredictedMoves();
            } else if(captRule.length != 0 && (selectedButton.getRow() != captRule[0] && selectedButton.getColumn() != captRule[1])){
                board[captRule[0]][captRule[1]].removeIcon();
                removeCaptured(captRule[0], captRule[1]);
                movePiece(clicked, predicts);
                clearPredictedMoves();
            }else {
                movePiece(clicked, predicts);
                clearPredictedMoves();
            }
        }
    }

    public native boolean isValidPieceToSelect(int row, int col);
    public static native char[][] placingPieces();
    public native void move(int from_row, int from_col, int to_row, int to_col, boolean changeTurn);
    public native void removeCaptured(int row, int col);
    public native HashMap<String, int[][]> predictedMoves(int row, int col);
    public native int[] captureRule(int rowDest, int colDest);
    public native void restart();
    public native char[][] displaying();
    public native boolean getTurn();

    //methods for tests
    public Cell[][] displayBoardTest(){
        return board;
    }
    public void setSelectedButton(Cell selected){
        selectedButton = selected;
    }
    public void displayJava(){
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


    public class CheckListener implements ActionListener{

        int row, col;

        public CheckListener(int row, int cols){
            this.row = row;
            this.col = cols;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(isKeyboardMode) {
                switchToMouseMode();
            }
            Cell clicked = board[row][col];

            if (selectedButton == null){
                if (isKeyboardMode) switchToMouseMode();
                if(!isValidPieceToSelect(row, col)){
                    selectedButton = null;
                }else if(isValidPieceToSelect(row, col)) {
                    HashMap<String, int[][]> predicts = predictedMoves(row, col);
                    selectedButton = clicked;
//                    System.out.println("color - " + selectedButton.getColor());
                    highlightPredictedMoves(predicts);
                }
            }else{

                checkingCapturesRules(clicked);
                clearPredictedMoves();
                selectedButton = null;
                if(winner() != 0){
                    showWinnerDialog(winner());
                }
            }
        }

    }
}
