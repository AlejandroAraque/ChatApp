import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatClient extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private JTextArea chatArea;
    private JTextField messageField;
    private ObjectOutputStream outputStream;
    private String username;

    public ChatClient() {
        this.username = JOptionPane.showInputDialog("Enter your username:");

        setTitle("Chat Client - " + username);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeUI();

        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Enviar el nombre de usuario al servidor
            outputStream.writeObject(username);

            new Thread(new ClientListener(socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(messageField, BorderLayout.SOUTH);
        add(sendButton, BorderLayout.EAST);
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            outputStream.writeObject(message);
            outputStream.flush();
            messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientListener implements Runnable {
        private Socket socket;
        private ObjectInputStream inputStream;

        public ClientListener(Socket socket) {
            this.socket = socket;
            try {
                this.inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Leer mensaje del servidor y mostrarlo en el área de chat
                    String message = (String) inputStream.readObject();
                    chatArea.append(message + "\n");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ChatClient chatClient = new ChatClient();
            chatClient.setVisible(true);
        });
    }
}
