import subprocess
import tkinter as tk
from tkinter import ttk

def perform_action():
    # Collect inputs from the GUI form
    operation = operation_var.get()
    algorithm = algorithm_var.get()
    mode = mode_var.get()
    key = key_entry.get()
    value = value_entry.get()

    try:
        # Build the Java command dynamically
        java_command = [
            "java", 
            "-cp", 
            "resources/secure-properties-tool.jar", 
            "com.mulesoft.tools.SecurePropertiesTool", 
            "string", 
            operation, 
            algorithm, 
            mode, 
            key, 
            value
        ]
        
        # Execute the Java command
        result = subprocess.run(java_command, capture_output=True, text=True)
        
        # Display the output in the GUI
        if result.returncode == 0:
            result_label.config(text=f"Result: {result.stdout.strip()}")
            copy_button.config(state=tk.NORMAL)  # Enable the copy button
            root.clipboard_clear()
            root.clipboard_append(result.stdout.strip())  # Save result to clipboard immediately
        else:
            result_label.config(text=f"Error: {result.stderr.strip()}")
            copy_button.config(state=tk.DISABLED)  # Disable the copy button
    except Exception as e:
        result_label.config(text=f"Error: {str(e)}")
        copy_button.config(state=tk.DISABLED)  # Disable the copy button

def copy_to_clipboard():
    # Copy the displayed result to the clipboard
    result_text = result_label.cget("text").replace("Result: ", "")  # Extract the result text
    root.clipboard_clear()
    root.clipboard_append(result_text)
    root.update()  # Ensure clipboard content is updated

# GUI setup
root = tk.Tk()
root.title("Encrypt/Decrypt Tool")
root.geometry("400x450")

# GUI elements for operation
tk.Label(root, text="Operation (encrypt/decrypt):").pack()
operation_var = tk.StringVar()
operation_dropdown = ttk.Combobox(root, textvariable=operation_var, values=["encrypt", "decrypt"])
operation_dropdown.pack()

# GUI elements for algorithm
tk.Label(root, text="Algorithm:").pack()
algorithm_var = tk.StringVar()
algorithm_dropdown = ttk.Combobox(root, textvariable=algorithm_var, values=["AES", "Blowfish", "DES", "DESed", "RCA", "RC2"])
algorithm_dropdown.pack()

# GUI elements for mode
tk.Label(root, text="Mode:").pack()
mode_var = tk.StringVar()
mode_dropdown = ttk.Combobox(root, textvariable=mode_var, values=["CBC", "CFB", "ECB", "OFB"])
mode_dropdown.pack()

# GUI elements for key
tk.Label(root, text="Key:").pack()
key_entry = ttk.Entry(root)
key_entry.pack()

# GUI elements for value
tk.Label(root, text="Value:").pack()
value_entry = ttk.Entry(root)
value_entry.pack()

# Submit button
submit_button = tk.Button(root, text="Run", command=perform_action)
submit_button.pack()

# Result label
result_label = tk.Label(root, text="")
result_label.pack()

# Copy to Clipboard button
copy_button = tk.Button(root, text="Copy to Clipboard", command=copy_to_clipboard, state=tk.DISABLED)  # Starts disabled
copy_button.pack()

root.mainloop()