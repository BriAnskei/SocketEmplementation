import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client {
    private JFrame frame;
    private JTextField messageField;
    private JTextArea chatArea;
    private JButton connectButton;
    private JButton sendButton;
    private Socket socket = null;
    private DataOutputStream out = null;
    private DataInputStream in = null;
    private String clientName;
    private boolean isConnected = false;

    public Client() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Client GUI");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());

        sendButton.addActionListener(e -> sendMessage());

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(connectButton, BorderLayout.NORTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void connectToServer() {
        if (isConnected) {
            JOptionPane.showMessageDialog(frame, "Already connected to the server!");
            return;
        }

        String address = JOptionPane.showInputDialog(frame, "Enter server address:", "127.0.0.1");
        String portStr = JOptionPane.showInputDialog(frame, "Enter server port:", "3000");
        clientName = JOptionPane.showInputDialog(frame, "Enter your name:", "Client");

        try {
            int port = Integer.parseInt(portStr);
            socket = new Socket(address, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            // Send the client name to the server
            out.writeUTF(clientName);

            chatArea.append("Connected to server as " + clientName + "\n");
            isConnected = true;
            connectButton.setEnabled(false);
            sendButton.setEnabled(true);

            // Start a thread to listen for server messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readUTF()) != null) {
                        chatArea.append(message + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server: " + e.getMessage() + "\n");
                    isConnected = false;
                    connectButton.setEnabled(true);
                    sendButton.setEnabled(false);
                }
            }).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(frame, "Not connected to a server!");
            return;
        }

        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            try {
                out.writeUTF(message); // Send message to server
                chatArea.append(message + "\n");
                messageField.setText("");
            } catch (IOException e) {
                chatArea.append("Failed to send message: " + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
