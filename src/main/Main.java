package main;

import javax.swing.*;

public class Main {

    static{
        System.load("/Users/silvia/CProjects/Project/cmake-build-debug/libProject.dylib");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(Board::new);

    }
}
