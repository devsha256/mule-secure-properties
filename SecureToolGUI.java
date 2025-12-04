import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SecureToolGUI extends JFrame {

    // Material Design Colors
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243); // Material Blue
    private static final Color PRIMARY_DARK = new Color(25, 118, 210);
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color ERROR_COLOR = new Color(211, 47, 47);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 18);

    // Components
    private JComboBox<String> operationCombo;
    private JComboBox<String> algorithmCombo;
    private JComboBox<String> modeCombo;
    private JTextField keyField;
    private JTextField valueField;
    private JTextArea resultArea;
    private JButton runButton;
    private JButton copyButton;
    private JLabel statusLabel;

    public SecureToolGUI() {
        setTitle("Mule Secure Properties Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);

        // Main Panel with Padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        // Header
        JLabel titleLabel = new JLabel("Encrypt/Decrypt Utility");
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Form Fields
        mainPanel.add(createLabel("Operation:"));
        operationCombo = createComboBox(new String[]{"encrypt", "decrypt"});
        mainPanel.add(operationCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createLabel("Algorithm:"));
        algorithmCombo = createComboBox(new String[]{"AES", "Blowfish", "DES", "DESed", "RCA", "RC2"});
        mainPanel.add(algorithmCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createLabel("Mode:"));
        modeCombo = createComboBox(new String[]{"CBC", "CFB", "ECB", "OFB"});
        mainPanel.add(modeCombo);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createLabel("Key:"));
        keyField = createTextField();
        mainPanel.add(keyField);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createLabel("Value:"));
        valueField = createTextField();
        mainPanel.add(valueField);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        runButton = createMaterialButton("RUN", PRIMARY_COLOR, Color.WHITE);
        runButton.addActionListener(e -> performAction());
        
        copyButton = createMaterialButton("COPY RESULT", Color.LIGHT_GRAY, Color.BLACK);
        copyButton.setEnabled(false);
        copyButton.addActionListener(e -> copyToClipboard());

        buttonPanel.add(runButton);
        buttonPanel.add(copyButton);
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Result Area
        mainPanel.add(createLabel("Result:"));
        resultArea = new JTextArea(4, 20);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        mainPanel.add(new JScrollPane(resultArea));
        
        // Status Bar
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(Color.GRAY);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(statusLabel);
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

        // Disable UI during processing
        setUIEnabled(false);
        statusLabel.setText("Processing...");
        resultArea.setText("");

        // Run in background thread (Fixes UI freezing issue)
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
                        resultArea.setForeground(TEXT_COLOR);
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
            // Robust path finding for the JAR
            String jarPath = "resources/secure-properties-tool.jar";
            if (!new File(jarPath).exists()) {
                // Try looking in current dir if resources folder doesn't exist
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
            pb.redirectErrorStream(true); // Merge stderr into stdout
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return output.toString().trim();
            } else {
                return "Error: " + output.toString().trim();
            }

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
        if (!enabled) copyButton.setEnabled(false);
    }

    private void showError(String message) {
        resultArea.setText(message);
        resultArea.setForeground(ERROR_COLOR);
        statusLabel.setText("Operation failed");
    }

    // --- UI Helper Methods ---

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MAIN_FONT);
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(MAIN_FONT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(MAIN_FONT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        box.setBackground(Color.WHITE);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        ((JComponent) box.getRenderer()).setBorder(new EmptyBorder(5, 5, 5, 5));
        return box;
    }

    private JButton createMaterialButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(isEnabled() ? bg : Color.LIGHT_GRAY);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        // Apply System Look and Feel for better OS integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new SecureToolGUI().setVisible(true);
        });
    }
}