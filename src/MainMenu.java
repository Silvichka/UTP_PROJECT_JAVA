import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainMenu extends JFrame {

    public MainMenu() {
        // Set up main menu window
        setTitle("Checkers Menu");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));  // Layout with 3 buttons in a vertical column

        // Start button
        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.PLAIN, 20));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the checkers board
                new Board();
                setVisible(false);  // Hide the main menu
            }
        });

        // Options button
        JButton optionsButton = new JButton("Options");
        optionsButton.setFont(new Font("Arial", Font.PLAIN, 20));
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a simple popup window (JDialog)
                JDialog optionsDialog = new JDialog(MainMenu.this, "Options", true);
                optionsDialog.setSize(200, 150);
                optionsDialog.setLayout(new FlowLayout());
                optionsDialog.add(new JLabel("Options Window (Empty)"));
                optionsDialog.setVisible(true);
            }
        });

        // Quit button
        JButton quitButton = new JButton("Quit");
        quitButton.setFont(new Font("Arial", Font.PLAIN, 20));
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);  // Exit the program
            }
        });

        // Add buttons to the frame
        add(startButton);
        add(optionsButton);
        add(quitButton);

        setLocationRelativeTo(null);  // Center the window on the screen
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::new);
    }
}

// Checkers board class
class Board extends JFrame {

    private final int SIZE = 8;
    private JButton[][] board;

    public Board() {
        // Set up the checkers board window
        setTitle("Checkers Game");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(SIZE, SIZE));
        setResizable(false);

        board = new JButton[SIZE][SIZE];
        initializeBoard();
        setVisible(true);

        // Add key listener for Esc key to open the in-game menu
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

    // Initialize the board with black and white tiles
    private void initializeBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                JButton button = new JButton();
                button.setOpaque(true);
                button.setBorderPainted(false);

                // Set alternating black and white squares
                if ((row + col) % 2 == 0) {
                    button.setBackground(Color.WHITE);
                } else {
                    button.setBackground(Color.BLACK);
                }

                // Add the button to the board
                board[row][col] = button;
                add(button);
            }
        }
    }

    // Show in-game menu when the Esc key is pressed
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
}