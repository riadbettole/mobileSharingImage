import socket
import threading
import tkinter as tk
from PIL import Image, ImageTk
import qrcode

# Server configuration
HOST = '192.168.11.103'
PORT = 6969

# Create a socket
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((HOST, PORT))
server_socket.listen()

# Function to handle client connections
def handle_client(client_socket):
    while True:
        try:
            # Receive data from the client
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break  # If no data is received, the client has disconnected

            # Display the received data in the GUI
            app.display_text(data)

            # Echo the data back to the client
            client_socket.send(data.encode('utf-8'))
        except Exception as e:
            print(f"Error: {e}")
            break

    # Close the client socket
    client_socket.close()

# Create and start a thread for handling connections
def start_server():
    while True:
        client_socket, addr = server_socket.accept()
        client_handler = threading.Thread(target=handle_client, args=(client_socket,))
        client_handler.start()

# Create a simple GUI
class ServerGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Server")

        self.label = tk.Label(root, text="Server is running on {}:{}".format(HOST, PORT))
        self.label.pack(pady=10)

        self.qr_code_label = tk.Label(root)
        self.qr_code_label.pack()

        self.text_label = tk.Label(root, text="Received text:")
        self.text_label.pack()

        # Generate QR code for the server address
        qr = qrcode.QRCode(version=1, box_size=10, border=5)
        qr.add_data("http://{}:{}".format(HOST, PORT))
        qr.make(fit=True)
        self.qr_img = qr.make_image(fill_color="black", back_color="white")
        self.qr_img = ImageTk.PhotoImage(self.qr_img)
        self.qr_code_label.configure(image=self.qr_img)
        self.qr_code_label.image = self.qr_img

    def display_text(self, text):
        # Display the received text in the GUI
        text_label = tk.Label(self.root, text=text)
        text_label.pack()

# Start the server thread
server_thread = threading.Thread(target=start_server)
server_thread.start()

# Create the GUI
root = tk.Tk()
app = ServerGUI(root)
root.mainloop()
