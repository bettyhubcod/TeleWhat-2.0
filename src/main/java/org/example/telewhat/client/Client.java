package org.example.telewhat.client;

import org.example.telewhat.entity.LectureNotification;
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
    private boolean onlineReceived = false;
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(Message message);
        void onUsersListReceived(List<String> connectes);
        void onOfflineUsersListReceived(List<String> offline);
        void onLectureNotificationReceived(String reader);
        void onConnectionLost();
    }

    public Client(String username, MessageListener listener) {
        this.username = username;
        this.messageListener = listener;
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("═══════════════════════════════════════");
        System.out.println("         TEST TOUTES LES RÈGLES        ");
        System.out.println("═══════════════════════════════════════");

        System.out.println("\n🔵 TEST RG3 : Double connexion");
        Client sidy1 = new Client("sidy ndiaye", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                System.out.println("📩 sidy reçoit : " + message.getContenue());
            }
            @Override
            public void onUsersListReceived(List<String> connectes) {
                System.out.println("👥 Users connectés : " + connectes);
            }
            @Override
            public void onOfflineUsersListReceived(List<String> offline) {
                System.out.println("⚫ Users offline : " + offline);
            }
            @Override
            public void onLectureNotificationReceived(String reader) {
                System.out.println("👁️ sidy : messages lus par " + reader);
            }
            @Override
            public void onConnectionLost() {
                System.out.println("❌ sidy : connexion perdue !");
            }
        });
        sidy1.connect("passer");

        Thread.sleep(500);

        Client sidy2 = new Client("sidy ndiaye", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {}
            @Override
            public void onUsersListReceived(List<String> connectes) {}
            @Override
            public void onOfflineUsersListReceived(List<String> offline) {}
            @Override
            public void onLectureNotificationReceived(String reader) {}
            @Override
            public void onConnectionLost() {}
        });
        sidy2.connect("passer");

        Thread.sleep(500);

        System.out.println("\n🔵 TEST RG6 : Message à user OFFLINE");
        Message msgOffline = new Message();
        msgOffline.setSender("sidy ndiaye");
        msgOffline.setReceveur("abdoulaye mathurin");
        msgOffline.setContenue("Salut, tu es offline mais tu recevras ce message !");
        msgOffline.setDateEnvoie(new Date());
        msgOffline.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgOffline);

        Thread.sleep(500);

        System.out.println("\n🔵 TEST RG7 : Message vide");
        Message msgVide = new Message();
        msgVide.setSender("sidy ndiaye");
        msgVide.setReceveur("abdoulaye mathurin");
        msgVide.setContenue("");
        msgVide.setDateEnvoie(new Date());
        msgVide.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgVide);

        Thread.sleep(500);

        System.out.println("\n🔵 TEST RG7 : Message trop long");
        Message msgLong = new Message();
        msgLong.setSender("sidy ndiaye");
        msgLong.setReceveur("abdoulaye mathurin");
        msgLong.setContenue("A".repeat(1001));
        msgLong.setDateEnvoie(new Date());
        msgLong.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgLong);

        Thread.sleep(500);

        System.out.println("\n🔵 TEST RG5 : Destinataire inexistant");
        Message msgInconnu = new Message();
        msgInconnu.setSender("sidy ndiaye");
        msgInconnu.setReceveur("userinconnu123");
        msgInconnu.setContenue("Ce message ne devrait pas passer");
        msgInconnu.setDateEnvoie(new Date());
        msgInconnu.setStatut(StatutMessage.ENVOYER);
        sidy1.sendMessage(msgInconnu);

        Thread.sleep(500);

        System.out.println("\n🔵 TEST RG6 : abdou se connecte → reçoit messages en attente");
        Client abdou = new Client("abdoulaye mathurin", new MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                System.out.println("📩 abdou reçoit de "
                        + message.getSender() + " : " + message.getContenue());
            }
            @Override
            public void onUsersListReceived(List<String> connectes) {
                System.out.println("👥 Users connectés : " + connectes);
            }
            @Override
            public void onOfflineUsersListReceived(List<String> offline) {
                System.out.println("⚫ Users offline : " + offline);
            }
            @Override
            public void onLectureNotificationReceived(String reader) {
                System.out.println("👁️ abdou : messages lus par " + reader);
            }
            @Override
            public void onConnectionLost() {
                System.out.println("❌ abdou : connexion perdue !");
            }
        });
        abdou.connect("passer");

        Thread.sleep(2000);

        System.out.println("\n🔵 TEST RG4 : Déconnexion");
        sidy1.disconnect();
        abdou.disconnect();

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("         FIN DES TESTS                 ");
        System.out.println("═══════════════════════════════════════");
    }

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
                        List<String> liste = (List<String>) received;
                        if (messageListener != null) {
                            if (!onlineReceived) {
                                messageListener.onUsersListReceived(liste);
                                onlineReceived = true;
                            } else {
                                messageListener.onOfflineUsersListReceived(liste);
                                onlineReceived = false;
                            }
                        }
                    } else if (received instanceof String) {
                        System.out.println("[SERVEUR] " + received);
                    } else if (received instanceof LectureNotification) {
                        LectureNotification notification = (LectureNotification) received;
                        if (messageListener != null) {
                            messageListener.onLectureNotificationReceived(notification.getReader());
                        }
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

    public void sendMessage(Message message) {
        if (!connected) {
            System.err.println("[CLIENT] Impossible d'envoyer : \n vous n'êtes pas connecté.");
            return;
        }
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("[CLIENT] Message envoyé à :\n " + message.getReceveur());
        } catch (IOException e) {
            connected = false;
            System.err.println("[CLIENT] Erreur d'envoi :\n " + e.getMessage());
            if (messageListener != null) messageListener.onConnectionLost();
        }
    }

    public void envoyerLectureNotification(String destinataire) {
        if (!connected) return;
        try {
            LectureNotification notification = new LectureNotification(username, destinataire);
            out.writeObject(notification);
            out.flush();
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur envoi notification lecture : " + e.getMessage());
        }
    }

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