package main;

import javax.swing.*;

public class Cell extends JButton {

    private int row;
    private int column;

    private char color = '.';

    public Cell(int row, int column){
        this.row = row;
        this.column = column;
    }

    public void setButtonWhitePiece(){
        ImageIcon icon = new ImageIcon("/Users/silvia/JavaProjects/UTP/Project/src/main/white_piece.png");
        this.setIcon(icon);
        color = 'W';
    }

    public void setButtonBlackPiece(){
        ImageIcon icon = new ImageIcon("/Users/silvia/JavaProjects/UTP/Project/src/main/black_piece.png");
        this.setIcon(icon);
        color = 'B';
    }

    public void removeIcon(){
        this.setIcon(null);
        color = '.';
    }

    public int getRow(){
        return row;
    }

    public int getColumn(){
        return column;
    }

    public char getColor(){
        return color;
    }
}
