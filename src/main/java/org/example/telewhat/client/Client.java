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
    // MAIN — Tests toutes les règles
    // ============================================================
    public static void main(String[] args) throws InterruptedException {

        System.out.println("═══════════════════════════════════════");
        System.out.println("         TEST TOUTES LES RÈGLES        ");
        System.out.println("═══════════════════════════════════════");

        // ============================================================
        // TEST RG3 : double connexion
        // ============================================================
        System.out.println("\n🔵 TEST RG3 : Double connexion");
        Client sidy1 = new Client("sidy ndiaye", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                System.out.println("📩 sidy reçoit : " + message.getContenue());
            }
            @Override
            public void onConnectionLost() {
                System.out.println("❌ sidy : connexion perdue !");
            }
        });
        sidy1.connect("passer");

        Thread.sleep(500);

        // Tentative double connexion → serveur répond automatiquement ERREUR
        Client sidy2 = new Client("sidy ndiaye", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {}
            @Override
            public void onConnectionLost() {}
        });
        sidy2.connect("passer");
        // ClientHandler envoie "ERREUR: Utilisateur déjà connecté" → connect() retourne false

        Thread.sleep(500);

        // ============================================================
        // TEST RG6 : message à abdoulayemathurin OFFLINE
        // ============================================================
        System.out.println("\n🔵 TEST RG6 : Message à user OFFLINE");
        Message msgOffline = new Message();
        msgOffline.setSender("sidy ndiaye");
        msgOffline.setReceveur("abdoulaye mathurin");
        msgOffline.setContenue("Salut, tu es offline mais tu recevras ce message !");
        msgOffline.setDateEnvoie(new Date());
        msgOffline.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgOffline);
        // ClientHandler sauvegarde en BD automatiquement

        Thread.sleep(500);

        // ============================================================
        // TEST RG7 : message vide
        // ============================================================
        System.out.println("\n🔵 TEST RG7 : Message vide");
        Message msgVide = new Message();
        msgVide.setSender("sidy ndiaye");
        msgVide.setReceveur("abdoulaye mathurin");
        msgVide.setContenue("");
        msgVide.setDateEnvoie(new Date());
        msgVide.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgVide);
        // ClientHandler répond "ERREUR: Message vide" → affiché dans startListenerThread

        Thread.sleep(500);

        // ============================================================
        // TEST RG7 : message trop long
        // ============================================================
        System.out.println("\n🔵 TEST RG7 : Message trop long");
        Message msgLong = new Message();
        msgLong.setSender("sidy ndiaye");
        msgLong.setReceveur("abdoulaye mathurin");
        msgLong.setContenue("A".repeat(1001));
        msgLong.setDateEnvoie(new Date());
        msgLong.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgLong);
        // ClientHandler répond "ERREUR: Message trop long"

        Thread.sleep(500);

        // ============================================================
        // TEST RG5 : destinataire inexistant
        // ============================================================
        System.out.println("\n🔵 TEST RG5 : Destinataire inexistant");
        Message msgInconnu = new Message();
        msgInconnu.setSender("sidy ndiaye");
        msgInconnu.setReceveur("userinconnu123");
        msgInconnu.setContenue("Ce message ne devrait pas passer");
        msgInconnu.setDateEnvoie(new Date());
        msgInconnu.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgInconnu);
        // ClientHandler répond "ERREUR: Destinataire introuvable"

        Thread.sleep(500);

        // ============================================================
        // TEST RG6 : abdou se connecte → reçoit messages en attente
        // ============================================================
        System.out.println("\n🔵 TEST RG6 : abdou se connecte → reçoit messages en attente");
        Client abdou = new Client("abdoulaye mathurin", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                System.out.println("📩 abdou reçoit de "
                        + message.getSender() + " : " + message.getContenue());
            }
            @Override
            public void onConnectionLost() {
                System.out.println("❌ abdou : connexion perdue !");
            }
        });
        abdou.connect("passer");
        // ClientHandler livre automatiquement les messages en attente (RG6)

        Thread.sleep(2000);

        // ============================================================
        // TEST RG4 : déconnexion → OFFLINE automatique via ClientHandler
        // ============================================================
        System.out.println("\n🔵 TEST RG4 : Déconnexion");
        sidy1.disconnect();
        abdou.disconnect();
        // ClientHandler appelle userService.deconnecter() → statut OFFLINE en BD

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("         FIN DES TESTS                 ");
        System.out.println("═══════════════════════════════════════");
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

                    if (received instanceof Message message) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived(message);
                        }
                    } else if (received instanceof List) {
                        List<String> connectes = (List<String>) received;
                        System.out.println("👥 Users connectés : " + connectes);
                    } else if (received instanceof String) {
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