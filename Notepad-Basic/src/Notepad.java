
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Notepad{

    private static boolean darkMode = false;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Notepad");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JLabel statusBar = new JLabel("Line: 1 Column: 1");
        frame.add(statusBar, BorderLayout.SOUTH);

        textArea.addCaretListener(e -> {
            try {
                int line = textArea.getLineOfOffset(textArea.getCaretPosition()) + 1;
                int col = textArea.getCaretPosition() - textArea.getLineStartOffset(line - 1) + 1;
                statusBar.setText("Line: " + line + " Column: " + col);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Handle exit confirmation
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit?", "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    frame.dispose();
                }
            }
        });

        JMenuBar menuBar = new JMenuBar();

        // ---------------- File Menu ----------------
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

        newItem.addActionListener(e -> textArea.setText(""));
        openItem.addActionListener(e -> openFile(frame, textArea));
        saveItem.addActionListener(e -> saveFile(frame, textArea));
        exitItem.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // ---------------- Edit Menu ----------------
        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem selectAllItem = new JMenuItem("Select All");

        undoItem.addActionListener(e -> { if (undoManager.canUndo()) undoManager.undo(); });
        redoItem.addActionListener(e -> { if (undoManager.canRedo()) undoManager.redo(); });
        cutItem.addActionListener(e -> textArea.cut());
        copyItem.addActionListener(e -> textArea.copy());
        pasteItem.addActionListener(e -> textArea.paste());
        selectAllItem.addActionListener(e -> textArea.selectAll());

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);

        // ---------------- View Menu ----------------
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Word Wrap", true);
        JCheckBoxMenuItem statusBarItem = new JCheckBoxMenuItem("Status Bar", true);
        JMenuItem darkModeItem = new JMenuItem("Toggle Dark Mode");

        wrapItem.addItemListener(e -> {
            textArea.setLineWrap(wrapItem.isSelected());
            textArea.setWrapStyleWord(wrapItem.isSelected());
        });

        statusBarItem.addItemListener(e -> statusBar.setVisible(statusBarItem.isSelected()));

        darkModeItem.addActionListener(e -> {
            darkMode = !darkMode;
            Color bg = darkMode ? Color.DARK_GRAY : Color.WHITE;
            Color fg = darkMode ? Color.WHITE : Color.BLACK;
            textArea.setBackground(bg);
            textArea.setForeground(fg);
            statusBar.setForeground(fg);
            statusBar.setBackground(bg);
        });

        viewMenu.add(wrapItem);
        viewMenu.add(statusBarItem);
        viewMenu.add(darkModeItem);

        // ---------------- Format Menu ----------------
        JMenu formatMenu = new JMenu("Format");
        JMenuItem fontItem = new JMenuItem("Change Font");

        fontItem.addActionListener(e -> {
            String fontName = JOptionPane.showInputDialog(frame, "Enter Font (e.g., Arial, Courier New):", "Change Font", JOptionPane.PLAIN_MESSAGE);
            if (fontName != null && !fontName.isEmpty()) {
                int size = 16;
                try {
                    size = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter Font Size:", "16"));
                } catch (NumberFormatException ignored) {}
                textArea.setFont(new Font(fontName, Font.PLAIN, size));
            }
        });

        formatMenu.add(fontItem);

        // Add all menus
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(formatMenu);
        frame.setJMenuBar(menuBar);

        frame.setVisible(true);
    }

    private static void openFile(JFrame frame, JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                textArea.read(reader, null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void saveFile(JFrame frame, JTextArea textArea) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                textArea.write(writer);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
