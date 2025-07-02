package com.mycompany.game;
//statement
import javax.swing.JFrame;

public class Game{
    public static void main(String[] args) {
        JFrame frame = new JFrame("Brick Breaker Deluxe");
        GamePlay game = new GamePlay();

        frame.setBounds(10, 10, 700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.add(game);
        frame.setVisible(true);

        game.requestFocusInWindow();
    }
}
