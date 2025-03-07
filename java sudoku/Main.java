import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static final int SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private JFrame frame;
    private JTextField[][] grid = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];

    private JLabel timerLabel;
    private Timer timer;
    private int secondsElapsed = 0;
    private boolean timerStarted = false;

    public Main() {
        frame = new JFrame("SUDOKU");
        frame.setLayout(new BorderLayout());
        frame.setSize(600, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Timer Label 
        timerLabel = new JLabel("Time: 00:00:00", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        frame.add(timerLabel, BorderLayout.NORTH);

        // Sudoku Grid
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new JTextField();
                grid[row][col].setHorizontalAlignment(JTextField.CENTER);
                grid[row][col].setFont(new Font("Arial", Font.BOLD, 20));

                // Bold borders for 3x3 subgrids
                int top = (row % SUBGRID_SIZE == 0) ? 3 : 1;
                int left = (col % SUBGRID_SIZE == 0) ? 3 : 1;
                int bottom = (row == SIZE - 1) ? 3 : 1;
                int right = (col == SIZE - 1) ? 3 : 1;
                grid[row][col].setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));

                gridPanel.add(grid[row][col]);
            }
        }
        frame.add(gridPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        JButton loadButton = new JButton("Load");
        JButton solveButton = new JButton("Solve");
        JButton saveButton = new JButton("Save");

        // Load Sudoku from file using FileChooser
        loadButton.addActionListener(e -> loadFromFile());

        // Solve Sudoku
        solveButton.addActionListener(e -> {
            readBoard();
            if (solveSudoku(0, 0)) {
                updateGrid();
                stopTimer(); // Stop timer when Sudoku is solved
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Inputs", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Save Sudoku to file using FileChooser
        saveButton.addActionListener(e -> saveToFile());

        buttonPanel.add(loadButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(saveButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void startTimer() {
        if (timerStarted) return; // Prevent multiple timers from starting

        timerStarted = true;
        secondsElapsed = 0;
        timerLabel.setText("Time: 00:00:00");

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
                timerLabel.setText("Time: " + formatTime(secondsElapsed));
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timerStarted = false;
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Scanner scanner = new Scanner(file)) {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        if (scanner.hasNextInt()) {
                            board[row][col] = scanner.nextInt();
                            if (board[row][col] != 0) {
                                grid[row][col].setText(String.valueOf(board[row][col]));
                                grid[row][col].setForeground(Color.BLUE);
                                grid[row][col].setEditable(false);
                            } else {
                                grid[row][col].setText("");
                                grid[row][col].setEditable(true);
                            }
                        }
                    }
                }
                stopTimer();  // Stop any previous timer
                startTimer(); // Start new timer when board loads
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error reading file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(frame);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        writer.print(board[row][col] + " ");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(frame, "Sudoku saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void readBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = grid[row][col].getText().trim();
                board[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
    }

    private void updateGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setText(String.valueOf(board[row][col]));
                grid[row][col].setForeground(Color.BLACK);
            }
        }
    }

    // Backtracking Sudoku Solver
    private boolean solveSudoku(int row, int col) {
        if (row == SIZE) return true;
        if (col == SIZE) return solveSudoku(row + 1, 0);
        if (board[row][col] != 0) return solveSudoku(row, col + 1);

        for (int num = 1; num <= SIZE; num++) {
            if (isValid(row, col, num)) {
                board[row][col] = num;
                if (solveSudoku(row, col + 1)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }

        int startRow = (row / SUBGRID_SIZE) * SUBGRID_SIZE;
        int startCol = (col / SUBGRID_SIZE) * SUBGRID_SIZE;
        for (int i = 0; i < SUBGRID_SIZE; i++) {
            for (int j = 0; j < SUBGRID_SIZE; j++) {
                if (board[startRow + i][startCol + j] == num) return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        new Main();
    }
    
}
