package org.example.telewhat.client;

import org.example.telewhat.entity.Message;
import org.example.telewhat.entity.User;
import org.example.telewhat.enumeration.StatutMessage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.List;

public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean connected = false;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(Message message);
        void onConnectionLost();
    }

    public Client(String username, MessageListener listener) {
        this.username = username;
        this.messageListener = listener;
    }

    // ============================================================
    // MAIN — pour tester sans JavaFX
    // ============================================================
    public static void main(String[] args) throws InterruptedException {

        Client client = new Client("oumou", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                System.out.println("📩 Message reçu de " + message.getSender()
                        + " : " + message.getContenue());
            }

            @Override
            public void onConnectionLost() {
                System.out.println("❌ Connexion perdue !");
            }
        });

        boolean connecte = client.connect("password123");

        if (connecte) {
            System.out.println("✅ Connecté avec succès !");

            // Envoyer un message à betty
            Message msg = new Message();
            msg.setSender("oumou");
            msg.setReceveur("betty");
            msg.setContenue("salut betty !");
            msg.setDateEnvoie(new Date());
            msg.setStatut(StatutMessage.ENVOYER);
            client.sendMessage(msg);

            System.out.println("📨 Message envoyé !");

            // Rester connecté pour recevoir des réponses
            Thread.sleep(10000);

            client.disconnect();
        } else {
            System.out.println("❌ Échec de la connexion !");
        }
    }

    // ============================================================
    // Connexion au serveur
    // ============================================================
    public boolean connect(String password) {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;

            User user = new User(username, password);
            out.writeObject(user);
            out.flush();

            String reponse = (String) in.readObject();
            if (reponse.equals("CONNEXION_OK")) {
                System.out.println("[CLIENT] Connexion acceptée ✅");
                startListenerThread();
                return true;
            } else {
                System.out.println("[CLIENT] Connexion refusée : " + reponse);
                return false;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[CLIENT] Erreur connexion : " + e.getMessage());
            return false;
        }
    }

    // ============================================================
    // Thread d'écoute des messages entrants
    // ============================================================
    private void startListenerThread() {
        Thread listenerThread = new Thread(() -> {
            while (connected) {
                try {
                    Object received = in.readObject();

                    // Cas 1 : c'est un message
                    if (received instanceof Message message) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived(message);
                        }
                    }

                    // Cas 2 : c'est la liste des connectés (Day 3)
                    else if (received instanceof List) {
                        List<String> connectes = (List<String>) received;
                        System.out.println("👥 Users connectés : " + connectes);
                        // Plus tard → afficher dans JavaFX
                    }

                    // Cas 3 : c'est une info ou erreur du serveur
                    else if (received instanceof String) {
                        System.out.println("[SERVEUR] " + received);
                    }

                } catch (SocketException e) {
                    if (connected) {
                        connected = false;
                        System.err.println("[CLIENT] Connexion perdue avec le serveur.");
                        if (messageListener != null) messageListener.onConnectionLost();
                    }
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (connected) {
                        connected = false;
                        System.err.println("[CLIENT] Erreur de réception : " + e.getMessage());
                        if (messageListener != null) messageListener.onConnectionLost();
                    }
                    break;
                }
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // ============================================================
    // Envoyer un message
    // ============================================================
    public void sendMessage(Message message) {
        if (!connected) {
            System.err.println("[CLIENT] Impossible d'envoyer : vous n'êtes pas connecté.");
            return;
        }
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("[CLIENT] Message envoyé à : " + message.getReceveur());
        } catch (IOException e) {
            connected = false;
            System.err.println("[CLIENT] Erreur d'envoi : " + e.getMessage());
            if (messageListener != null) messageListener.onConnectionLost();
        }
    }

    // ============================================================
    // Déconnexion
    // ============================================================
    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.writeObject("DECONNEXION");
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("[CLIENT] Déconnecté du serveur.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() { return connected; }
    public String getUsername() { return username; }
}