package org.example.telewhat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private static final int PORT = 8080;

    // Annuaire global de tous les clients connectés
    public static ConcurrentHashMap<String, ClientHandler> clientsConnectes = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Serveur démarré sur le port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                System.out.println("En attente d'un client...");

                Socket socket = serverSocket.accept();
                System.out.println("Nouveau client : " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            System.out.println(" Erreur serveur : " + e.getMessage());
        }

    }
}