import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends JFrame { // Extend JFrame to make it compatible with WindowBuilder
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ConcurrentHashMap<String, Socket> clientMap = new ConcurrentHashMap<>();
    private Thread serverThread;

    /**
     * @wbp.parser.entryPoint
     */
    public Server() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Server GUI");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(3000);
            isRunning = true;
            logArea.append("Server started on port 3000\n");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);

            serverThread = new Thread(() -> {
                while (isRunning) {
                    try {
                        logArea.append("Waiting for a client...\n");
                        Socket socket = serverSocket.accept();
                        logArea.append("A new client connected\n");

                        // Handle the client in a new thread
                        new Thread(() -> handleClient(socket)).start();
                    } catch (IOException e) {
                        if (isRunning) {
                            logArea.append("Error accepting client: " + e.getMessage() + "\n");
                        }
                    }
                }
            });
            serverThread.start();
        } catch (IOException e) {
            logArea.append("Error starting server: " + e.getMessage() + "\n");
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String clientName = in.readUTF();
            clientMap.put(clientName, socket);
            logArea.append(clientName + " has joined.\n");

            broadcast(clientName + " has joined the chat.", clientName);

            String message;
            while ((message = in.readUTF()) != null) {
                logArea.append(clientName + ": " + message + "\n");
                broadcast(clientName + ": " + message, clientName);
            }
        } catch (IOException e) {
            logArea.append("Connection closed: " + e.getMessage() + "\n");
        }
    }

    private void broadcast(String message, String sender) {
        clientMap.forEach((name, clientSocket) -> {
            if (!name.equals(sender)) {
                try {
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.writeUTF(message);
                } catch (IOException e) {
                    logArea.append("Error sending message to " + name + ": " + e.getMessage() + "\n");
                }
            }
        });
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverThread != null) {
                serverThread.interrupt();
            }
            clientMap.clear();
            logArea.append("Server stopped.\n");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        } catch (IOException e) {
            logArea.append("Error stopping server: " + e.getMessage() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
