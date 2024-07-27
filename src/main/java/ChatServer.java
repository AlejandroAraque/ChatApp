import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 5555;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;
        private String username;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Pedir al cliente que ingrese un nombre de usuario
                outputStream.writeObject("Enter your username: ");
                this.username = (String) inputStream.readObject();
                broadcast(username + " joined the chat.");

                while (true) {
                    // Leer mensaje del cliente
                    String message = (String) inputStream.readObject();

                    // Manejo del comando para salir
                    if (message.equalsIgnoreCase("exit")) {
                        broadcast(username + " left the chat.");
                        break;
                    }

                    // Enviar el mensaje a todos los clientes conectados con el nombre del remitente
                    broadcast(username + ": " + message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                // Remover el cliente al desconectarse
                clients.remove(this);
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            // Enviar el mensaje a todos los clientes conectados
            for (ClientHandler client : clients) {
                try {
                    client.outputStream.writeObject(message);
                    client.outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}