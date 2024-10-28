import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CheckersGameGUI extends JFrame {
    private static final int BOARD_SIZE = 8;
    private static final char EMPTY = '.';
    private static final char RED_PIECE = 'r';
    private static final char BLACK_PIECE = 'b';
    private static final char RED_KING = 'R';
    private static final char BLACK_KING = 'B';

    private char[][] board;
    private boolean redTurn;
    private JButton[][] buttons;
    private int[] selectedPiece = null;
    private List<int[]> predictedMoves = new ArrayList<>();

    public CheckersGameGUI() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        buttons = new JButton[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        redTurn = true;

        setTitle("Checkers Game");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        initializeButtons();
        updateBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if ((row + col) % 2 != 0) {
                    if (row < 3) board[row][col] = BLACK_PIECE;
                    else if (row > 4) board[row][col] = RED_PIECE;
                    else board[row][col] = EMPTY;
                } else {
                    board[row][col] = EMPTY;
                }
            }
        }
    }

    private void initializeButtons() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JButton button = new JButton();
                button.setOpaque(true);
                button.setBorderPainted(false);
                button.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.DARK_GRAY);

                final int r = row, c = col;
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleButtonClick(r, c);
                    }
                });

                buttons[row][col] = button;
                add(button);
            }
        }
    }

    private void handleButtonClick(int row, int col) {
        if (selectedPiece == null) { // Selecting piece
            if (isValidSelection(row, col)) {
                selectedPiece = new int[]{row, col};
                buttons[row][col].setBackground(Color.YELLOW);
                highlightPredictedMoves(row, col);
            }
        } else { // Moving piece
            int startX = selectedPiece[0], startY = selectedPiece[1];
            if (move(startX, startY, row, col)) {
                redTurn = !redTurn;
            }
            selectedPiece = null;
            resetButtonColors();
            updateBoard();
        }
    }

    private void highlightPredictedMoves(int row, int col) {
        predictedMoves.clear();
        char piece = board[row][col];
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] direction : directions) {
            int endX = row + direction[0];
            int endY = col + direction[1];

            // Single step move
            if (isValidMove(piece, row, col, endX, endY, direction[0], direction[1])) {
                predictedMoves.add(new int[]{endX, endY});
            }

            // Capture move (jump)
            endX = row + 2 * direction[0];
            endY = col + 2 * direction[1];
            if (isValidMove(piece, row, col, endX, endY, 2 * direction[0], 2 * direction[1])) {
                predictedMoves.add(new int[]{endX, endY});
            }
        }

        for (int[] move : predictedMoves) {
            buttons[move[0]][move[1]].setBackground(Color.YELLOW);
        }
    }

    private boolean isValidSelection(int row, int col) {
        char piece = board[row][col];
        return (redTurn && (piece == RED_PIECE || piece == RED_KING)) ||
                (!redTurn && (piece == BLACK_PIECE || piece == BLACK_KING));
    }

    private boolean move(int startX, int startY, int endX, int endY) {
        char piece = board[startX][startY];
        int deltaX = endX - startX;
        int deltaY = endY - startY;

        if (isValidMove(piece, startX, startY, endX, endY, deltaX, deltaY)) {
            board[startX][startY] = EMPTY;
            board[endX][endY] = piece;
            promoteToKing(piece, endX, endY);

            if (Math.abs(deltaX) == 2) {
                int capturedX = (startX + endX) / 2;
                int capturedY = (startY + endY) / 2;
                board[capturedX][capturedY] = EMPTY;
            }
            return true;
        }
        return false;
    }

    private boolean isValidMove(char piece, int startX, int startY, int endX, int endY, int deltaX, int deltaY) {
        if (endX < 0 || endX >= BOARD_SIZE || endY < 0 || endY >= BOARD_SIZE || board[endX][endY] != EMPTY) {
            return false;
        }

        if (Math.abs(deltaX) == 1 && Math.abs(deltaY) == 1) {
            return (piece == RED_PIECE && deltaX == -1) || (piece == BLACK_PIECE && deltaX == 1) || isKing(piece);
        } else if (Math.abs(deltaX) == 2 && Math.abs(deltaY) == 2) {
            int capturedX = (startX + endX) / 2;
            int capturedY = (startY + endY) / 2;
            char capturedPiece = board[capturedX][capturedY];
            return ((piece == RED_PIECE && deltaX == -2) || (piece == BLACK_PIECE && deltaX == 2) || isKing(piece)) &&
                    ((redTurn && (capturedPiece == BLACK_PIECE || capturedPiece == BLACK_KING)) ||
                            (!redTurn && (capturedPiece == RED_PIECE || capturedPiece == RED_KING)));
        }
        return false;
    }

    private boolean isKing(char piece) {
        return piece == RED_KING || piece == BLACK_KING;
    }

    private void promoteToKing(char piece, int x, int y) {
        if (piece == RED_PIECE && x == 0) board[x][y] = RED_KING;
        else if (piece == BLACK_PIECE && x == BOARD_SIZE - 1) board[x][y] = BLACK_KING;
    }

    private void resetButtonColors() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.DARK_GRAY);
            }
        }
    }

    private void updateBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                char piece = board[row][col];
                buttons[row][col].setText(pieceToText(piece));
            }
        }

        if (isGameOver()) {
            JOptionPane.showMessageDialog(this, (redTurn ? "Black" : "Red") + " wins!");
            System.exit(0);
        }
    }

    private String pieceToText(char piece) {
        switch (piece) {
            case RED_PIECE:
                return "r";
            case BLACK_PIECE:
                return "b";
            case RED_KING:
                return "R";
            case BLACK_KING:
                return "B";
            default:
                return "";
        }
    }

    private boolean hasNoMovesLeft(char piece) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == piece || board[row][col] == (piece == RED_PIECE ? RED_KING : BLACK_KING)) {
                    if (canMoveOrCapture(row, col)) return false;
                }
            }
        }
        return true;
    }

    private boolean canMoveOrCapture(int startX, int startY) {
        int[][] moves = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}, {-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] move : moves) {
            int endX = startX + move[0], endY = startY + move[1];
            if (isValidMove(board[startX][startY], startX, startY, endX, endY, move[0], move[1])) return true;
        }
        return false;
    }

    private boolean isGameOver() {
        return hasNoMovesLeft(RED_PIECE) || hasNoMovesLeft(BLACK_PIECE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CheckersGameGUI game = new CheckersGameGUI();
            game.setVisible(true);
        });
    }
}
