package com.mycompany.game;
//Abstract Window Toolkit
import java.awt.*;

public class MapGenerator {
    public int[][] map;
    public int brickWidth;
    public int brickHeight;
    private final Color levelColor;
//constractor  calling
    public MapGenerator(int row, int col, int level) {
        map = new int[row][col];
        for (int[] rowArr : map) {
            for (int j = 0; j < col; j++) {
                rowArr[j] = 1;
            }
        }

        brickWidth = 540 / col;
        brickHeight = 150 / row;

        Color[] colors = {
            new Color(0xFF8787), new Color(0x87CEFA),
            new Color(0x90EE90), new Color(0xFFA500),
            new Color(0x9370DB)
        };
        levelColor = colors[(level - 1) % colors.length];
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    int x = j * brickWidth + 80;
                    int y = i * brickHeight + 50;

                    g.setColor(levelColor);
                    g.fillRect(x, y, brickWidth, brickHeight);

                    g.setStroke(new BasicStroke(4));
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, brickWidth, brickHeight);
                }
            }
        }
    }

    public void setBrickValue(int value, int row, int col) {
        map[row][col] = value;
    }
}
