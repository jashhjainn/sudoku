import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class Main extends JFrame {
    private static final int SIZE = 9; // Size of the Sudoku grid
    private JTextField[][] grid = new JTextField[SIZE][SIZE]; // Grid for user input
    private int[][] board = new int[SIZE][SIZE]; // Backend board to store numbers

    // Constructor to set up the GUI
    public Main() {
        setTitle("Sudoku");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the Sudoku grid
        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col] = new JTextField();
                grid[row][col].setHorizontalAlignment(JTextField.CENTER);
                grid[row][col].setFont(new Font("Arial", Font.BOLD, 20));

                // Set borders to outline 3x3 subgrids in bold black
                int top = (row % 3 == 0) ? 3 : 1; // Thicker border at the top of each 3x3 subgrid
                int left = (col % 3 == 0) ? 3 : 1; // Thicker border at the left of each 3x3 subgrid
                int bottom = (row == SIZE - 1) ? 3 : 1; // Thicker border at the bottom of the grid
                int right = (col == SIZE - 1) ? 3 : 1; // Thicker border at the right of the grid

                grid[row][col].setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.BLACK));
                gridPanel.add(grid[row][col]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        // Create buttons for loading, solving, and saving
        JPanel buttonPanel = new JPanel();
        JButton loadButton = new JButton("Load");
        JButton solveButton = new JButton("Solve");
        JButton saveButton = new JButton("Save");

        // Load Sudoku from a file
        loadButton.addActionListener(e -> loadFromFile());

        // Solve the Sudoku
        solveButton.addActionListener(e -> {
            readBoard(); // Read input from the grid
            if (solveSudoku(0, 0)) { // Solve the puzzle
                updateGrid(); // Update the grid with the solution
            } else {
                JOptionPane.showMessageDialog(this, "No solution exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Save the Sudoku to a file
        saveButton.addActionListener(e -> saveToFile());

        buttonPanel.add(loadButton);
        buttonPanel.add(solveButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Load Sudoku from a file
    private void loadFromFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Scanner scanner = new Scanner(file)) {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        if (scanner.hasNextInt()) {
                            board[row][col] = scanner.nextInt();
                            grid[row][col].setText(String.valueOf(board[row][col]));
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Save Sudoku to a file
    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (int row = 0; row < SIZE; row++) {
                    for (int col = 0; col < SIZE; col++) {
                        writer.print(board[row][col] + " ");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "Sudoku saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Read the board from the grid
    private void readBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                String text = grid[row][col].getText().trim();
                board[row][col] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }
    }

    // Update the grid with the solved board
    private void updateGrid() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                grid[row][col].setText(String.valueOf(board[row][col]));
            }
        }
    }

    // Backtracking Sudoku solver
    private boolean solveSudoku(int row, int col) {
        if (row == SIZE) return true; // If all rows are filled, puzzle is solved
        if (col == SIZE) return solveSudoku(row + 1, 0); // Move to the next row
        if (board[row][col] != 0) return solveSudoku(row, col + 1); // Skip filled cells

        // Try numbers 1 to 9
        for (int num = 1; num <= SIZE; num++) {
            if (isValid(row, col, num)) {
                board[row][col] = num; // Place the number
                if (solveSudoku(row, col + 1)) return true; // Recursively solve
                board[row][col] = 0; // Backtrack if the solution is invalid
            }
        }
        return false; // No valid number found
    }

    // Check if a number is valid in a given cell
    private boolean isValid(int row, int col, int num) {
        // Check row and column
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }

        // Check 3x3 subgrid
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num) return false;
            }
        }
        return true;
    }

    // Main method to start the program
    public static void main(String[] args) {
        new Main();
    }
}