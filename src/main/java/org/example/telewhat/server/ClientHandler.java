package org.example.telewhat.server;

import org.example.telewhat.auth.AuthService;
import org.example.telewhat.entity.Message;
import org.example.telewhat.entity.User;
import org.example.telewhat.service.UserService;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private User userConnecte;

    // Les services d'Oumou
    private AuthService authService = new AuthService();
    private UserService userService = new UserService();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            System.out.println("✅ ClientHandler démarré pour : " + socket.getInetAddress());

            while (true) {
                Object objetRecu = ois.readObject();

                if (objetRecu instanceof User) {
                    gererConnexion((User) objetRecu);
                }
                else if (objetRecu instanceof Message) {
                    gererMessage((Message) objetRecu);
                }
                else if (objetRecu instanceof String) {
                    String commande = (String) objetRecu;
                    if (commande.equalsIgnoreCase("DECONNEXION")) {
                        gererDeconnexion();
                        break;
                    }
                }
            }

        } catch (IOException e) {
            // RG10 : perte de connexion brutale
            System.out.println("⚠️ Connexion perdue avec : " +
                    (userConnecte != null ? userConnecte.getUsername() : socket.getInetAddress()));
            gererDeconnexion();

        } catch (ClassNotFoundException e) {
            System.out.println("❌ Objet inconnu reçu : " + e.getMessage());

        } finally {
            fermerRessources();
        }
    }

    private void gererConnexion(User user) throws IOException {
        System.out.println("🔑 Tentative de connexion : " + user.getUsername());

        // RG3 : un seul utilisateur connecté à la fois
        if (Server.clientsConnectes.containsKey(user.getUsername())) {
            oos.writeObject("ERREUR: Utilisateur déjà connecté");
            System.out.println("❌ " + user.getUsername() + " déjà connecté");
            return;
        }

        // RG2 : vérifier les identifiants via AuthService d'Oumou
        // authService.login() → cherche en base + vérifie BCrypt + met ONLINE
        boolean authentifie = authService.login(user.getUsername(), user.getPassword());

        if (!authentifie) {
            oos.writeObject("ERREUR: Identifiants incorrects");
            System.out.println("❌ Échec authentification : " + user.getUsername());
            return;
        }

        // Authentification OK → on enregistre dans l'annuaire
        this.userConnecte = user;
        Server.clientsConnectes.put(user.getUsername(), this);

        // RG12 : journaliser la connexion
        System.out.println("✅ " + user.getUsername() + " connecté. Total : "
                + Server.clientsConnectes.size());

        oos.writeObject("CONNEXION_OK");
    }

    private void gererMessage(Message message) throws IOException {

        // RG2 : doit être authentifié
        if (userConnecte == null) {
            oos.writeObject("ERREUR: Non authentifié");
            return;
        }

        // RG7 : message non vide
        if (message.getContenue() == null || message.getContenue().isBlank()) {
            oos.writeObject("ERREUR: Message vide");
            return;
        }

        // RG7 : max 1000 caractères
        if (message.getContenue().length() > 1000) {
            oos.writeObject("ERREUR: Message trop long (max 1000 caractères)");
            return;
        }

        // RG5 : vérifier que le destinataire existe en base via UserService d'Oumou
        if (!userService.userExists(message.getReceveur())) {
            oos.writeObject("ERREUR: Destinataire introuvable");
            return;
        }

        // RG12 : journaliser l'envoi
        System.out.println("📨 " + message.getSender() + " → " + message.getReceveur());

        // Chercher le destinataire dans l'annuaire
        ClientHandler destinataireHandler = Server.clientsConnectes.get(message.getReceveur());

        // RG5 : destinataire connecté → livraison directe
        if (destinataireHandler != null) {
            destinataireHandler.envoyerObjet(message);
            System.out.println("✅ Message livré à " + message.getReceveur());
        }

        // RG6 : destinataire hors ligne → sauvegarder pour plus tard
        else {
            System.out.println("💾 " + message.getReceveur() + " hors ligne, message sauvegardé");
            // TODO : MessageService.sauvegarder(message) → Betty s'en charge jour 2
        }
    }

    private void gererDeconnexion() {
        if (userConnecte != null) {

            // Retirer de l'annuaire
            Server.clientsConnectes.remove(userConnecte.getUsername());

            // RG4 : passer OFFLINE via UserService d'Oumou
            userService.deconnecter(userConnecte.getUsername());

            // RG12 : journaliser
            System.out.println("🔴 " + userConnecte.getUsername()
                    + " déconnecté. Total : " + Server.clientsConnectes.size());

            userConnecte = null;
        }
    }

    public void envoyerObjet(Object objet) throws IOException {
        oos.writeObject(objet);
        oos.flush();
    }

    private void fermerRessources() {
        try {
            if (ois != null) ois.close();
            if (oos != null) oos.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("❌ Erreur fermeture ressources : " + e.getMessage());
        }
    }
}