import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SecureToolGUI extends JFrame {

    // --- CONFIGURATION CONSTANTS ---
    private static final String DEFAULT_KEY = "YourSecretKeyHere";
    private static final String DEFAULT_ALGORITHM = "Blowfish";
    private static final String DEFAULT_MODE = "CBC";

    // --- THEME COLORS ---
    // Light Mode
    private static final Color LIGHT_BG = new Color(250, 250, 250);
    private static final Color LIGHT_TEXT = new Color(33, 33, 33);
    private static final Color LIGHT_INPUT_BG = Color.WHITE;
    private static final Color LIGHT_BORDER = new Color(200, 200, 200);

    // Dark Mode
    private static final Color DARK_BG = new Color(48, 48, 48);
    private static final Color DARK_TEXT = new Color(238, 238, 238);
    private static final Color DARK_INPUT_BG = new Color(66, 66, 66);
    private static final Color DARK_BORDER = new Color(100, 100, 100);

    // Accents
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243); // Blue
    private static final Color ERROR_COLOR = new Color(229, 57, 53);     // Red

    // Fonts
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);

    // State
    private boolean isDarkMode = false;

    // Components
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JLabel titleLabel;
    private List<JLabel> labels = new ArrayList<>(); 
    private JComboBox<String> operationCombo;
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> modeCombo;
    private JTextField keyField;
    private JTextField valueField;
    private JTextArea resultArea;
    private JScrollPane resultScrollPane;
    private JButton runButton;
    private JButton copyButton;
    private JLabel statusLabel;
    private JButton themeToggle; // Changed to JButton for custom icon

    public SecureToolGUI() {
        setTitle("Mule Secure Properties Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLocationRelativeTo(null);

        // Initialize Main Panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        add(mainPanel);

        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titleLabel = new JLabel("Encrypt/Decrypt Utility");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        
        // Theme Toggle Icon Button
        themeToggle = createThemeButton();
        themeToggle.addActionListener(e -> toggleTheme());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(themeToggle, BorderLayout.EAST);
        
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Form Fields ---
        addLabel("Operation:");
        operationCombo = createComboBox(new String[]{"encrypt", "decrypt"});
        mainPanel.add(operationCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        addLabel("Algorithm:");
        algorithmCombo = createComboBox(new String[]{"AES", "Blowfish", "DES", "DESed", "RCA", "RC2"});
        algorithmCombo.setSelectedItem(DEFAULT_ALGORITHM);
        mainPanel.add(algorithmCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        addLabel("Mode:");
        modeCombo = createComboBox(new String[]{"CBC", "CFB", "ECB", "OFB"});
        modeCombo.setSelectedItem(DEFAULT_MODE);
        mainPanel.add(modeCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        addLabel("Key:");
        keyField = createTextField();
        keyField.setText(DEFAULT_KEY);
        mainPanel.add(keyField);
        mainPanel.add(Box.createVerticalStrut(10));

        addLabel("Value:");
        valueField = createTextField();
        mainPanel.add(valueField);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Action Buttons ---
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        runButton = createMaterialButton("RUN");
        runButton.setBackground(PRIMARY_COLOR);
        runButton.setForeground(Color.WHITE);
        runButton.addActionListener(e -> performAction());
        
        copyButton = createMaterialButton("COPY RESULT");
        copyButton.setEnabled(false);
        copyButton.setBackground(Color.LIGHT_GRAY);
        copyButton.setForeground(Color.BLACK);
        copyButton.addActionListener(e -> copyToClipboard());

        buttonPanel.add(runButton);
        buttonPanel.add(copyButton);
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // --- Result Area ---
        addLabel("Result:");
        resultArea = new JTextArea(4, 20);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setEditable(false);
        
        resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(resultScrollPane);
        
        // --- Status Bar ---
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(statusLabel);

        // Apply initial theme
        refreshTheme();
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        refreshTheme();
    }

    private void refreshTheme() {
        Color bg = isDarkMode ? DARK_BG : LIGHT_BG;
        Color text = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        Color inputBg = isDarkMode ? DARK_INPUT_BG : LIGHT_INPUT_BG;
        Color border = isDarkMode ? DARK_BORDER : LIGHT_BORDER;

        // Containers
        getContentPane().setBackground(bg);
        mainPanel.setBackground(bg);
        buttonPanel.setBackground(bg);

        // Labels
        for (JLabel lbl : labels) {
            lbl.setForeground(text);
        }
        statusLabel.setForeground(isDarkMode ? Color.GRAY : Color.GRAY);

        // Inputs & Combos
        updateInputStyle(keyField, inputBg, text, border);
        updateInputStyle(valueField, inputBg, text, border);
        updateInputStyle(resultArea, inputBg, text, border);
        resultScrollPane.setBorder(new LineBorder(border, 1));
        
        updateComboStyle(operationCombo, inputBg, text, border);
        updateComboStyle(algorithmCombo, inputBg, text, border);
        updateComboStyle(modeCombo, inputBg, text, border);

        // Update Toggle Button style
        themeToggle.setBackground(isDarkMode ? DARK_INPUT_BG : LIGHT_BG);
        themeToggle.repaint(); // Force redraw for icon change
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JButton createThemeButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw Button Background (Circle)
                Color bg = getBackground();
                if (getModel().isRollover()) {
                    bg = isDarkMode ? bg.brighter() : bg.darker();
                }
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());

                // Draw Icon
                int size = getWidth();
                if (isDarkMode) {
                    // Draw Sun Icon (Yellow/Orange)
                    g2.setColor(new Color(255, 193, 7)); // Amber
                    int r = size / 4;
                    int c = size / 2;
                    g2.fillOval(c - r, c - r, r * 2, r * 2);
                    
                    // Rays
                    g2.setStroke(new BasicStroke(2));
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        int x1 = c + (int)(Math.cos(angle) * (r + 2));
                        int y1 = c + (int)(Math.sin(angle) * (r + 2));
                        int x2 = c + (int)(Math.cos(angle) * (r + 6));
                        int y2 = c + (int)(Math.sin(angle) * (r + 6));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    // Draw Moon Icon (Dark Gray/Blue)
                    g2.setColor(new Color(66, 66, 66)); // Dark Gray
                    
                    // Create Crescent using constructive geometry (subtract circle from circle)
                    int d = size / 2; // diameter
                    int offset = size / 6;
                    
                    Area moon = new Area(new Ellipse2D.Double(offset, offset, d, d));
                    Area shadow = new Area(new Ellipse2D.Double(offset + (d/3.0), offset - (d/6.0), d, d));
                    moon.subtract(shadow);
                    
                    g2.translate(size/4, size/4); // Center it roughly
                    g2.fill(moon);
                }
                g2.dispose();
            }
        };
        
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Toggle Dark/Light Mode");
        
        return btn;
    }

    private void updateInputStyle(javax.swing.text.JTextComponent comp, Color bg, Color fg, Color border) {
        comp.setBackground(bg);
        comp.setForeground(fg);
        comp.setCaretColor(fg);
        comp.setBorder(new CompoundBorder(new LineBorder(border, 1), new EmptyBorder(5, 8, 5, 8)));
    }

    private void updateComboStyle(JComboBox<?> box, Color bg, Color fg, Color border) {
        box.setBackground(bg);
        box.setForeground(fg);
        box.setUI(new BasicComboBoxUI()); 
        ((JComponent) box.getRenderer()).setBackground(bg);
        ((JComponent) box.getRenderer()).setForeground(fg);
    }

    private void performAction() {
        String operation = (String) operationCombo.getSelectedItem();
        String algorithm = (String) algorithmCombo.getSelectedItem();
        String mode = (String) modeCombo.getSelectedItem();
        String key = keyField.getText().trim();
        String value = valueField.getText().trim();

        if (key.isEmpty() || value.isEmpty()) {
            showError("Key and Value cannot be empty.");
            return;
        }

        setUIEnabled(false);
        statusLabel.setText("Processing...");
        resultArea.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return executeJar(operation, algorithm, mode, key, value);
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                try {
                    String result = get();
                    if (result.startsWith("Error:")) {
                        showError(result);
                    } else {
                        resultArea.setText(result);
                        resultArea.setForeground(isDarkMode ? DARK_TEXT : LIGHT_TEXT);
                        statusLabel.setText("Success");
                        copyButton.setEnabled(true);
                        copyButton.setBackground(PRIMARY_COLOR);
                        copyButton.setForeground(Color.WHITE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    showError("Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String executeJar(String op, String algo, String mode, String key, String val) {
        try {
            String jarPath = "resources/secure-properties-tool.jar";
            if (!new File(jarPath).exists()) {
                if (new File("secure-properties-tool.jar").exists()) {
                    jarPath = "secure-properties-tool.jar";
                } else {
                    return "Error: JAR file not found at " + jarPath;
                }
            }

            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-cp");
            command.add(jarPath);
            command.add("com.mulesoft.tools.SecurePropertiesTool");
            command.add("string");
            command.add(op);
            command.add(algo);
            command.add(mode);
            command.add(key);
            command.add(val);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();

            return (exitCode == 0) ? output.toString().trim() : "Error: " + output.toString().trim();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void copyToClipboard() {
        String selection = resultArea.getText();
        if (selection != null && !selection.isEmpty()) {
            StringSelection data = new StringSelection(selection);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(data, data);
            statusLabel.setText("Copied to clipboard!");
        }
    }

    private void setUIEnabled(boolean enabled) {
        runButton.setEnabled(enabled);
        keyField.setEnabled(enabled);
        valueField.setEnabled(enabled);
        operationCombo.setEnabled(enabled);
        algorithmCombo.setEnabled(enabled);
        modeCombo.setEnabled(enabled);
        themeToggle.setEnabled(enabled);
        if (!enabled) copyButton.setEnabled(false);
    }

    private void showError(String message) {
        resultArea.setText(message);
        resultArea.setForeground(ERROR_COLOR);
        statusLabel.setText("Operation failed");
    }

    // --- UI Helpers ---

    private void addLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MAIN_FONT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        labels.add(label);
        mainPanel.add(label);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(MAIN_FONT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(MAIN_FONT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    private JButton createMaterialButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bgColor = getBackground();
                if (!isEnabled()) bgColor = Color.LIGHT_GRAY;
                else if (getModel().isPressed()) bgColor = bgColor.darker();
                else if (getModel().isRollover()) bgColor = bgColor.brighter();
                
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SecureToolGUI().setVisible(true));
    }
}