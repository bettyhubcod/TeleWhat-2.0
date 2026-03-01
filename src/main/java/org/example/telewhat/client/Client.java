package org.example.telewhat.client;

import org.example.telewhat.entity.Message;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class Client {

    // Adresse et port du serveur (doit être le même que Server.java de Betty)
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private Socket socket;                  // La connexion au serveur
    private ObjectOutputStream out;         // Pour envoyer des objets au serveur
    private ObjectInputStream in;           // Pour recevoir des objets du serveur

    private String username;                // Le nom de l'utilisateur connecté
    private boolean connected = false;      // Est-ce qu'on est connecté ?

    // Interface pour notifier JavaFX quand un message arrive ou connexion perdue
    private MessageListener messageListener;

    // ============================================================
    // Interface MessageListener
    // Le Controller JavaFX va implémenter cette interface
    // ============================================================
    public interface MessageListener {
        void onMessageReceived(Message message);  // Quand on reçoit un message
        void onConnectionLost();                   // Quand on perd la connexion (RG10)
    }

    // ============================================================
    // Constructeur
    // ============================================================
    public Client(String username, MessageListener listener) {
        this.username = username;
        this.messageListener = listener;
    }

    // ============================================================
    // Connexion au serveur
    // ============================================================
    public boolean connect() {
        try {
            // Ouvrir la connexion avec le serveur
            socket = new Socket(SERVER_HOST, SERVER_PORT);

            // IMPORTANT : out avant in (sinon deadlock)
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            connected = true;

            // S'identifier auprès du serveur en envoyant le username
            out.writeObject(username);
            out.flush();

            // Démarrer le thread qui écoute les messages entrants
            startListenerThread();

            System.out.println("[CLIENT] Connecté au serveur en tant que : " + username);
            return true;

        } catch (IOException e) {
            // RG10 : erreur de connexion
            System.err.println("[CLIENT] Impossible de se connecter au serveur : " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // Thread d'écoute des messages entrants
    // Tourne en arrière-plan pendant toute la session
    // ============================================================
    private void startListenerThread() {
        Thread listenerThread = new Thread(() -> {
            while (connected) {
                try {
                    // Attendre un objet du serveur
                    Object received = in.readObject();

                    // Si c'est un Message, notifier l'interface JavaFX
                    if (received instanceof Message message) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived(message);
                        }
                    }

                } catch (SocketException e) {
                    // Perte de connexion réseau (RG10)
                    if (connected) {
                        connected = false;
                        System.err.println("[CLIENT] Connexion perdue avec le serveur.");
                        if (messageListener != null) {
                            messageListener.onConnectionLost();
                        }
                    }
                    break;

                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        connected = false;
                        System.err.println("[CLIENT] Erreur de réception : " + e.getMessage());
                        if (messageListener != null) {
                            messageListener.onConnectionLost();
                        }
                    }
                    break;
                }
            }
        });

        listenerThread.setDaemon(true); // S'arrête automatiquement quand l'app se ferme
        listenerThread.start();
    }

    // ============================================================
    // Envoyer un message au serveur
    // ============================================================
    public void sendMessage(Message message) {
        // RG2 : vérifier que le client est connecté
        if (!connected) {
            System.err.println("[CLIENT] Impossible d'envoyer : vous n'êtes pas connecté.");
            return;
        }
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("[CLIENT] Message envoyé à : " + message.getReceveur());

        } catch (IOException e) {
            // Si l'envoi échoue, la connexion est perdue (RG10)
            connected = false;
            System.err.println("[CLIENT] Erreur d'envoi : " + e.getMessage());
            if (messageListener != null) {
                messageListener.onConnectionLost();
            }
        }
    }

    // ============================================================
    // Déconnexion propre du serveur
    // ============================================================
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[CLIENT] Déconnecté du serveur.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // Getters utiles
    // ============================================================
    public boolean isConnected() {
        return connected;
    }

    public String getUsername() {
        return username;
    }
}