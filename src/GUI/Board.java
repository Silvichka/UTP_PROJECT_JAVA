package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JFrame{

    private final int SIZE = 8;

    private JButton[][] board;
    private JButton selectedButton;

    public Board(){
        setTitle("Checkers");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(SIZE, SIZE));
        setResizable(false);

        board = new JButton[SIZE][SIZE];
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
        for (int row = 0; row < SIZE; row++){
            for (int cols = 0; cols < SIZE; cols++){
                JButton button = new JButton();
                button.setOpaque(true);
                button.setBorderPainted(false);

                if ((row + cols) % 2 == 0){
                    button.setBackground(Color.WHITE);
                }else{
                    button.setBackground(Color.BLACK);
                }

//                button.addActionListener(new CheckListener(row, cols));

                board[row][cols] = button;
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

    // Reset the board to its initial state
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

//    private class CheckListener implements ActionListener{
//
//        int row, cols;
//
//        public CheckListener(int row, int cols){
//            this.row = row;
//            this.cols = cols;
//        }
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            JButton clicked = board[row][cols];
//
//            if (selectedButton == null){
//                if(isValidPieceToSelect(clicked)) {
//                    selectedButton = clicked;
//                    clicked.setBackground(Color.LIGHT_GRAY);
//                }
//            }else{
//                if (isValidMove(row, cols)){
//                    movePiece(clicked);
//                }else {
//                    selectedButton.setBackground(Color.BLACK);
//                    selectedButton = null;
//                }
//            }
//        }
//
//        private boolean isValidPieceToSelect(JButton button);
//        private boolean isValidMove(int destRow, int destCol);
//
//        private void movePiece(JButton destButton){
//            destButton.setText(selectedButton.getText());
//            destButton.setForeground(selectedButton.getForeground());
//            selectedButton.setText("");
//            selectedButton.setBackground(Color.BLACK);  // Reset the background color
//
//            selectedButton = null;
//        }
//
//    }


}
