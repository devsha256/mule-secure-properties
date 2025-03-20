# Encrypt/Decrypt Utility Tool

A simple, cross-platform utility tool with a minimalist user interface for string encryption and decryption. This tool leverages the **Mule Secure Properties Tool** for secure processing of strings and supports various encryption algorithms and modes.

---

## Features

- **Cross-Platform Support**: Compatible with macOS, Windows, and Linux.
- **User-Friendly Interface**: Intuitive form with fields for operation, algorithm, mode, key, and value.
- **Clipboard Functionality**: Copy the encryption/decryption result directly to your clipboard.
- **Encryption & Decryption**: Securely process strings using algorithms like AES, Blowfish, DES, etc.

---

## Supported Algorithms and Modes

### **Algorithms**
- AES
- Blowfish
- DES
- DESed
- RCA
- RC2

### **Modes**
- CBC
- CFB
- ECB
- OFB

---

## Prerequisites

- **Java Runtime Environment (JRE)**: Ensure Java is installed and accessible in your system's PATH.
- Download the `secure-properties-tool.jar` file from MuleSoft.

---

## Installation

### **macOS**
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/encrypt-decrypt-tool.git
   cd encrypt-decrypt-tool
   ```
2. Install required dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Run the application:
   ```bash
   python encrypt_decrypt_tool.py
   ```

4. (Optional) Package as a macOS app using `PyInstaller`:
   ```bash
   pyinstaller --onefile --windowed encrypt_decrypt_tool.py
   ```

### **Windows/Linux**
Follow the same steps as macOS. Ensure dependencies and `java` are properly installed.

---

## Usage

1. **Launch the Application**:
   Run the Python script to open the GUI.

2. **Fill out the form**:
   - Select **Operation**: `encrypt` or `decrypt`.
   - Choose the **Algorithm** and **Mode** from dropdowns.
   - Enter the **Key** and **Value** for encryption/decryption.

3. **Run the Tool**:
   Click the **Run** button to process the input.

4. **Copy the Result**:
   Use the **Copy to Clipboard** button to save the output for immediate use.

---

## Example Command (Backend)
The tool executes the following Java command in the background:
```bash
java -cp secure-properties-tool.jar com.mulesoft.tools.SecurePropertiesTool string encrypt Blowfish CBC mulesoft "some value to encrypt"
```
