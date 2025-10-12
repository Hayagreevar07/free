import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;

public class TicTacToeGUI extends JFrame implements ActionListener {
    private JButton[][] buttons = new JButton[3][3];
    private char currentPlayer = 'X';
    private int xWins = 0, oWins = 0, draws = 0;
    private JLabel scoreLabel;
    private boolean vsComputer = false;
    private String difficulty = "Easy";

    public TicTacToeGUI() {
        setTitle("🎮 Tic Tac Toe");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        setUIFont(new FontUIResource(new Font("Segoe UI", Font.PLAIN, 14)));

        // Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem resetScores = new JMenuItem("Reset Scores");
        JMenuItem exit = new JMenuItem("Exit");

        newGame.addActionListener(e -> resetBoard());
        resetScores.addActionListener(e -> resetScores());
        exit.addActionListener(e -> System.exit(0));

        gameMenu.add(newGame);
        gameMenu.add(resetScores);
        gameMenu.addSeparator();
        gameMenu.add(exit);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // Game Mode Selection
        int choice = JOptionPane.showOptionDialog(this,
                "Choose Game Mode:",
                "Tic Tac Toe",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Player vs Player", "Player vs Computer"},
                "Player vs Player");

        vsComputer = (choice == 1);

        if (vsComputer) {
            String[] levels = {"Easy", "Medium", "Hard"};
            difficulty = (String) JOptionPane.showInputDialog(
                    this,
                    "Choose Difficulty:",
                    "Difficulty",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    levels,
                    "Easy"
            );
        }

        // Scoreboard
        JPanel topPanel = new JPanel(new BorderLayout());
        scoreLabel = new JLabel("X Wins: 0 | O Wins: 0 | Draws: 0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(245, 245, 245));
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(scoreLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // Game Board
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setBackground(new Color(230, 230, 230));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font("Segoe UI", Font.BOLD, 60));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].setForeground(new Color(33, 33, 33));
                buttons[i][j].setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2));
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        if (!btn.getText().equals("")) return;

        btn.setText(String.valueOf(currentPlayer));
        playSound("click.wav");

        if (checkWin()) {
            playSound("win.wav");
            if (currentPlayer == 'X') xWins++; else oWins++;
            JOptionPane.showMessageDialog(this, currentPlayer + " wins! 🎉");
            askPlayAgain();
            return;
        } else if (isBoardFull()) {
            playSound("draw.wav");
            draws++;
            JOptionPane.showMessageDialog(this, "It's a Draw! 🤝");
            askPlayAgain();
            return;
        }

        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';

        if (vsComputer && currentPlayer == 'O') computerMove();
    }

    // ---------------- AI ----------------
    private void computerMove() {
        switch (difficulty) {
            case "Easy": randomMove(); break;
            case "Medium": smartMove(); break;
            case "Hard": bestMove(); break;
        }

        if (checkWin()) {
            playSound("win.wav");
            oWins++;
            JOptionPane.showMessageDialog(this, "Computer wins! 💻");
            askPlayAgain();
            return;
        } else if (isBoardFull()) {
            playSound("draw.wav");
            draws++;
            JOptionPane.showMessageDialog(this, "It's a Draw!");
            askPlayAgain();
            return;
        }

        currentPlayer = 'X';
    }

    private void randomMove() {
        java.util.List<JButton> empty = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (buttons[i][j].getText().equals("")) empty.add(buttons[i][j]);
        if (!empty.isEmpty()) empty.get(new Random().nextInt(empty.size())).setText("O");
    }

    private void smartMove() {
        if (tryWinOrBlock("O")) return;
        if (tryWinOrBlock("X")) return;
        randomMove();
    }

    private boolean tryWinOrBlock(String player) {
        for (int i = 0; i < 3; i++) {
            if (canWin(buttons[i][0], buttons[i][1], buttons[i][2], player)) return true;
            if (canWin(buttons[0][i], buttons[1][i], buttons[2][i], player)) return true;
        }
        return canWin(buttons[0][0], buttons[1][1], buttons[2][2], player)
            || canWin(buttons[0][2], buttons[1][1], buttons[2][0], player);
    }

    private boolean canWin(JButton b1, JButton b2, JButton b3, String player) {
        if (b1.getText().equals(player) && b2.getText().equals(player) && b3.getText().equals("")) { b3.setText("O"); return true; }
        if (b1.getText().equals(player) && b3.getText().equals(player) && b2.getText().equals("")) { b2.setText("O"); return true; }
        if (b2.getText().equals(player) && b3.getText().equals(player) && b1.getText().equals("")) { b1.setText("O"); return true; }
        return false;
    }

    private void bestMove() {
        int bestScore = Integer.MIN_VALUE;
        JButton best = null;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText("O");
                    int score = minimax(false);
                    buttons[i][j].setText("");
                    if (score > bestScore) { bestScore = score; best = buttons[i][j]; }
                }
        if (best != null) best.setText("O");
    }

    private int minimax(boolean isMax) {
        if (checkWinner("O")) return 1;
        if (checkWinner("X")) return -1;
        if (isBoardFull()) return 0;

        int bestScore = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText(isMax ? "O" : "X");
                    int score = minimax(!isMax);
                    buttons[i][j].setText("");
                    bestScore = isMax ? Math.max(score, bestScore) : Math.min(score, bestScore);
                }
        return bestScore;
    }

    // ---------------- Game Logic ----------------
    private boolean checkWin() { return checkWinner("X") || checkWinner("O"); }

    private boolean checkWinner(String player) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(player) &&
                buttons[i][1].getText().equals(player) &&
                buttons[i][2].getText().equals(player)) { highlight(buttons[i][0], buttons[i][1], buttons[i][2]); return true; }
            if (buttons[0][i].getText().equals(player) &&
                buttons[1][i].getText().equals(player) &&
                buttons[2][i].getText().equals(player)) { highlight(buttons[0][i], buttons[1][i], buttons[2][i]); return true; }
        }
        if (buttons[0][0].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][2].getText().equals(player)) { highlight(buttons[0][0], buttons[1][1], buttons[2][2]); return true; }
        if (buttons[0][2].getText().equals(player) &&
            buttons[1][1].getText().equals(player) &&
            buttons[2][0].getText().equals(player)) { highlight(buttons[0][2], buttons[1][1], buttons[2][0]); return true; }
        return false;
    }

    private void highlight(JButton b1, JButton b2, JButton b3) {
        Color winColor = new Color(144, 238, 144);
        b1.setBackground(winColor);
        b2.setBackground(winColor);
        b3.setBackground(winColor);
    }

    private boolean isBoardFull() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                if (b.getText().equals("")) return false;
        return true;
    }

    private void askPlayAgain() {
        updateScoreboard();
        int choice = JOptionPane.showConfirmDialog(this, "Play Again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) resetBoard();
        else System.exit(0);
    }

    private void resetBoard() {
        for (JButton[] row : buttons)
            for (JButton b : row) {
                b.setText("");
                b.setBackground(Color.WHITE);
            }
        currentPlayer = 'X';
    }

    private void updateScoreboard() {
        scoreLabel.setText("X Wins: " + xWins + " | O Wins: " + oWins + " | Draws: " + draws);
    }

    private void resetScores() {
        xWins = oWins = draws = 0;
        updateScoreboard();
    }

    // ---------------- Sound ----------------
    private void playSound(String fileName) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new java.io.File(fileName)));
            clip.start();
        } catch (Exception e) { System.out.println("Sound error: " + e.getMessage()); }
    }

    // ---------------- Utility ----------------
    public static void setUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) UIManager.put(key, f);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TicTacToeGUI::new);
    }
}
