import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EncryptDecryptTool {
    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Encrypt/Decrypt Tool");
        frame.setSize(1920, 1080); // Large frame size for spacing
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set a GridBagLayout for center alignment
        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10); // Padding around components
        constraints.fill = GridBagConstraints.HORIZONTAL; // Expand components horizontally
        constraints.gridwidth = GridBagConstraints.REMAINDER; // Take the full row width

        // Add GUI components
        JLabel operationLabel = createLabel("Operation:");
        String[] operations = {"encrypt", "decrypt"};
        JComboBox<String> operationDropdown = new JComboBox<>(operations);

        JLabel algorithmLabel = createLabel("Algorithm:");
        String[] algorithms = {"AES", "Blowfish", "DES", "DESed", "RCA", "RC2"};
        JComboBox<String> algorithmDropdown = new JComboBox<>(algorithms);

        JLabel modeLabel = createLabel("Mode:");
        String[] modes = {"CBC", "CFB", "ECB", "OFB"};
        JComboBox<String> modeDropdown = new JComboBox<>(modes);

        JLabel keyLabel = createLabel("Key:");
        JTextField keyField = new JTextField(30);

        JLabel valueLabel = createLabel("Value:");
        JTextField valueField = new JTextField(30);

        JLabel resultLabel = createLabel("Result:");
        JTextArea resultArea = new JTextArea(5, 40); // Larger result area
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JButton runButton = createButton("Run");
        JButton copyButton = createButton("Copy to Clipboard");
        copyButton.setEnabled(false);

        // Action Listener for Run Button
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String operation = operationDropdown.getSelectedItem().toString();
                String algorithm = algorithmDropdown.getSelectedItem().toString();
                String mode = modeDropdown.getSelectedItem().toString();
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();
                value = value.replaceAll("[^A-Za-z0-9+/=]", ""); // Remove invalid Base64 characters

        
                try {
                    // Build the command
                    String command = String.format(
                        "java -cp resources/secure-properties-tool.jar com.mulesoft.tools.SecurePropertiesTool string %s %s %s %s \"%s\"",
                        operation, algorithm, mode, key, value
                    );
        
                    // Debug: Print the command and input details
                    resultArea.setText("Running command:\n" + command + "\n");
        
                    // Execute the command
                    Process process = Runtime.getRuntime().exec(command);
        
                    // Capture stdout (standard output)
                    BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder stdout = new StringBuilder();
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        stdout.append(line).append("\n");
                    }
        
                    // Capture stderr (errors)
                    BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder stderr = new StringBuilder();
                    while ((line = stderrReader.readLine()) != null) {
                        stderr.append(line).append("\n");
                    }
        
                    process.waitFor(); // Wait for the process to finish
        
                    // Display output or errors
                    if (process.exitValue() == 0) {
                        resultArea.setText(stdout.toString().trim());
                        copyButton.setEnabled(true);
                    } else {
                        resultArea.setText(stderr.toString().trim());
                        copyButton.setEnabled(false);
                    }
        
                } catch (Exception ex) {
                    resultArea.setText("Exception:\n" + ex.getMessage());
                    copyButton.setEnabled(false);
                }
            }
        });

        // Action Listener for Copy Button
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = resultArea.getText();
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new java.awt.datatransfer.StringSelection(result), null
                );
            }
        });

        // Add components to the frame using constraints
        frame.add(operationLabel, constraints);
        frame.add(operationDropdown, constraints);
        frame.add(algorithmLabel, constraints);
        frame.add(algorithmDropdown, constraints);
        frame.add(modeLabel, constraints);
        frame.add(modeDropdown, constraints);
        frame.add(keyLabel, constraints);
        frame.add(keyField, constraints);
        frame.add(valueLabel, constraints);
        frame.add(valueField, constraints);
        frame.add(runButton, constraints);
        frame.add(copyButton, constraints);
        frame.add(resultLabel, constraints);
        frame.add(scrollPane, constraints);

        // Set color-blind friendly background and foreground colors
        frame.getContentPane().setBackground(new Color(240, 240, 240)); // Light gray background
        resultArea.setBackground(new Color(230, 230, 250)); // Light lavender
        resultArea.setForeground(new Color(0, 0, 128)); // Dark blue text

        // Display the frame
        frame.setVisible(true);
    }

    // Method to create labels with color-blind friendly colors
    private static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(0, 0, 128)); // Dark blue text
        return label;
    }

    // Method to create buttons with color-blind friendly colors
    private static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(255, 255, 204)); // Soft yellow buttons
        button.setForeground(new Color(0, 0, 128)); // Dark blue text
        return button;
    }
}
