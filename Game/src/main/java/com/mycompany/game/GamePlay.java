package com.mycompany.game;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;
import java.util.*;
import javax.swing.Timer;

public class GamePlay extends JPanel implements KeyListener, ActionListener {
    private enum GameState { START, PLAYING, PAUSED, GAME_OVER }
    private GameState currentState = GameState.START;

    private boolean play = false;
    private int score = 0, level = 1, lives = 3, totalBricks;
    
    private final int delay = 8;

    private int playerX = 310;
    private int paddleWidth = 100;

    private int ballposX = 120, ballposY = 350;
    private int ballXdir = -1, ballYdir = -2;

    private MapGenerator map;
    private Clip backgroundClip;
    private boolean isMusicMuted = false;

    private final java.util.List<PowerUp> powerUps = new ArrayList<>();

    public GamePlay() {
        setupLevel();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        Timer timer = new javax.swing.Timer(delay, this);
        timer.start();
        playBackgroundMusic("background_music.wav");
    }

    private void setupLevel() {
        map = new MapGenerator(level + 2, 7, level);
        totalBricks = (level + 2) * 7;
    }

    private void resetBall() {
        ballposX = 120;
        ballposY = 350;
        playerX = 310;
        paddleWidth = 100;
        ballXdir = -1 - level / 3;
        ballYdir = -2 - level / 3;
    }

    private void playBackgroundMusic(String fileName) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(fileName));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audio);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.println("Could not play background music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    private void toggleMusic() {
        if (backgroundClip != null) {
            if (isMusicMuted) {
                backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
                isMusicMuted = false;
            } else {
                backgroundClip.stop();
                isMusicMuted = true;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.YELLOW);
        g.fillRect(1, 1, 692, 592);

        if (currentState == GameState.START) {
            g.setFont(new Font("MV Boli", Font.BOLD, 30));
            g.setColor(Color.BLACK);
            g.drawString("Press ENTER to Start", 200, 300);
            return;
        }

        map.draw((Graphics2D) g);
        g.setColor(Color.BLUE);
        g.fillRect(playerX, 550, paddleWidth, 12);
        g.setColor(Color.RED);
        g.fillOval(ballposX, ballposY, 20, 20);

        g.setColor(Color.BLACK);
        g.setFont(new Font("MV Boli", Font.BOLD, 22));
        g.drawString("Score: " + score, 520, 30);
        g.drawString("Level: " + level, 420, 30);
        g.drawString("Lives: " + lives, 50, 30);

        for (PowerUp p : powerUps) p.draw(g);

        if (currentState == GameState.PAUSED) {
            g.setFont(new Font("MV Boli", Font.BOLD, 28));
            g.drawString("Game Paused", 250, 280);
        }

        if (currentState == GameState.GAME_OVER) {
            g.setFont(new Font("MV Boli", Font.BOLD, 30));
            g.drawString("Game Over, Score: " + score, 160, 300);
            g.setFont(new Font("MV Boli", Font.BOLD, 20));
            g.drawString("Press Enter to Restart", 230, 350);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState != GameState.PLAYING) {
            repaint();
            return;
        }

        Rectangle paddleRect = new Rectangle(playerX, 550, paddleWidth, 12);
        Rectangle ballRect = new Rectangle(ballposX, ballposY, 20, 20);

        if (ballRect.intersects(paddleRect)) {
            ballYdir = -ballYdir;
        }

        outer:
        for (int i = 0; i < map.map.length; i++) {
            for (int j = 0; j < map.map[0].length; j++) {
                if (map.map[i][j] > 0) {
                    int brickX = j * map.brickWidth + 80;
                    int brickY = i * map.brickHeight + 50;
                    Rectangle brickRect = new Rectangle(brickX, brickY, map.brickWidth, map.brickHeight);

                    if (ballRect.intersects(brickRect)) {
                        map.setBrickValue(0, i, j);
                        totalBricks--;
                        score += 5;

                        if (Math.random() < 0.25) {
                            PowerUp.Type type = PowerUp.Type.random();
                            powerUps.add(new PowerUp(type, brickX + map.brickWidth / 2, brickY));
                        }

                        if (ballposX + 19 <= brickRect.x || ballposX + 1 >= brickRect.x + brickRect.width)
                            ballXdir = -ballXdir;
                        else
                            ballYdir = -ballYdir;
                        break outer;
                    }
                }
            }
        }

        ballposX += ballXdir;
        ballposY += ballYdir;

        if (ballposX < 0 || ballposX > 670) ballXdir = -ballXdir;
        if (ballposY < 0) ballYdir = -ballYdir;

        if (ballposY > 570) {
            lives--;
            if (lives > 0) resetBall();
            else {
                currentState = GameState.GAME_OVER;
                play = false;
                stopBackgroundMusic();
            }
        }

        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp p = iterator.next();
            p.update();

            if (p.getBounds().intersects(paddleRect)) {
                applyPowerUp(p.type);
                iterator.remove();
            } else if (p.y > 600) {
                iterator.remove();
            }
        }

        if (totalBricks <= 0) {
            level++;
            score += 50;
            setupLevel();
            resetBall();
        }

        repaint();
    }

    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case EXPAND -> paddleWidth = Math.min(paddleWidth + 40, 200);
            case SHRINK -> paddleWidth = Math.max(paddleWidth - 30, 40);
            case SPEED_UP -> {
                ballXdir *= 1.5;
                ballYdir *= 1.5;
            }
            case SLOW_DOWN -> {
                ballXdir *= 0.7;
                ballYdir *= 0.7;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && playerX + paddleWidth < 700) playerX += 50;
        if (e.getKeyCode() == KeyEvent.VK_LEFT && playerX > 10) playerX -= 50;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentState == GameState.START || currentState == GameState.GAME_OVER) {
                score = 0;
                level = 1;
                lives = 3;
                setupLevel();
                resetBall();
                play = true;
                currentState = GameState.PLAYING;
                playBackgroundMusic("background_music.wav");
                repaint();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            if (currentState == GameState.PLAYING)
                currentState = GameState.PAUSED;
            else if (currentState == GameState.PAUSED)
                currentState = GameState.PLAYING;
        }

        if (e.getKeyCode() == KeyEvent.VK_M) {
            toggleMusic();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // === Inner PowerUp class ===
    public static class PowerUp {
        public enum Type {
            EXPAND, SHRINK, SPEED_UP, SLOW_DOWN;

            public static Type random() {
                Type[] values = Type.values();
                return values[new Random().nextInt(values.length)];
            }
        }

                public Type type;
        public int x, y;

        public PowerUp(Type type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }

        public void update() {
            y += 2; // Falling speed
        }

        public void draw(Graphics g) {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(type.name().substring(0, 1), x + 6, y + 15);
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, 20, 20);
        }
    }
}
