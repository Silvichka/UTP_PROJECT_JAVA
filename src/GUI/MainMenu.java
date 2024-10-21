package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
}
