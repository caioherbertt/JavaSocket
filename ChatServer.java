package Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start(12345);
    }

    public void start(int port) {
        try {
            // Cria o socket do servidor na porta especificada
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor iniciado na porta " + port);

            while (true) {
                // Aguarda a conexão de um cliente
                Socket socket = serverSocket.accept();
                System.out.println("Novo cliente conectado: " + socket.getInetAddress().getHostAddress());

                // Cria uma nova instância do tratador de cliente e o inicia em uma nova thread
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void broadcastMessage(String name, String message, ClientHandler sender) {
        System.out.println(name + ": " + message);
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(name + ": " + message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                name = in.readLine(); // Lê o nome do cliente enviado pelo cliente
                // Verifica se o nome já está sendo usado
                if (isNameTaken(name)) {
                    out.println("Nome de usuário já em uso. Por favor, escolha outro nome.");
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (name == null) {
                return; // Encerra a execução se o nome não for válido
            }
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(name, message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
    
    private synchronized boolean isNameTaken(String name) {
        for (ClientHandler client : clients) {
            if (client.name != null && client.name.equals(name)) {
                return true;
            }
        }
        return false;
    }
}