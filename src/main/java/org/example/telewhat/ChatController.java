package org.example.telewhat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.telewhat.client.Client;
import org.example.telewhat.entity.Message;
import org.example.telewhat.enumeration.StatutMessage;
import org.example.telewhat.service.MessageService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatController {

    @FXML private VBox messagesBox;
    @FXML private TextField messageInput;
    @FXML private ImageView backgroundImage;
    @FXML private ListView<String> onlineUsersList;
    @FXML private ListView<String> offlineUsersList;
    @FXML private Label headerLabel;

    private Client client;
    private String username;
    private String destinataire;
    private MessageService messageService = new MessageService();
    private Map<String, List<Node>> discussions = new HashMap<>();

    @FXML
    public void initialize() {
        Image img = new Image(getClass().getResourceAsStream("/images/bg-green.jpeg"));
        backgroundImage.setImage(img);

        onlineUsersList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("  " + item);
                    setStyle("-fx-text-fill: #E3EED4; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 0 8 4; -fx-background-color: transparent;");
                }
            }
        });

        offlineUsersList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("  " + item);
                    setStyle("-fx-text-fill: #AEC3B0; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 0 8 4; -fx-background-color: transparent;");
                }
            }
        });

        onlineUsersList.setOnMouseClicked(event -> {
            String selected = onlineUsersList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals(username)) {
                changerDestinataire(selected);
            }
        });

        offlineUsersList.setOnMouseClicked(event -> {
            String selected = offlineUsersList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals(username)) {
                changerDestinataire(selected);
            }
        });
    }

    private void changerDestinataire(String nouvelUtilisateur) {
        if (destinataire != null) {
            discussions.put(destinataire, new ArrayList<>(messagesBox.getChildren()));
        }

        destinataire = nouvelUtilisateur;
        messagesBox.getChildren().clear();
        headerLabel.setText("Connecté : " + username + " — discussion " + destinataire);

        // Toujours recharger depuis la BD pour avoir tous les messages
        List<Message> historique = messageService.getHistorique(username, destinataire);
        for (Message msg : historique) {
            boolean estMoi = msg.getSender().equals(username);
            afficherMessage(msg.getSender() + " :\n" + msg.getContenue(), estMoi);
        }

        // Ajouter les messages reçus en temps réel qui ne sont pas encore en BD
        if (discussions.containsKey(destinataire)) {
            // Les messages en temps réel sont déjà dans discussions
            // On les ajoute seulement s'ils ne sont pas déjà dans l'historique BD
            discussions.remove(destinataire);
        }

        discussions.put(destinataire, new ArrayList<>(messagesBox.getChildren()));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        client = new Client(username, new Client.MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                Platform.runLater(() -> {
                    String expediteur = message.getSender();
                    String texte = expediteur + " :\n" + message.getContenue();

                    // Créer le noeud du message
                    HBox conteneur = new HBox();
                    conteneur.setPadding(new Insets(4, 8, 4, 8));
                    conteneur.setAlignment(Pos.CENTER_LEFT);

                    Label label = new Label(texte);
                    label.setWrapText(true);
                    label.setMaxWidth(400);
                    label.setPadding(new Insets(8, 12, 8, 12));
                    label.setStyle("-fx-background-color: #0F2A1D; -fx-text-fill: #E3EED4; " +
                            "-fx-background-radius: 16 16 16 4; -fx-font-size: 13px;");
                    conteneur.getChildren().add(label);

                    // Sauvegarder dans la Map de cet expéditeur
                    discussions.computeIfAbsent(expediteur, k -> new ArrayList<>()).add(conteneur);

                    // Si on est déjà en train de discuter avec cet expéditeur → afficher directement
                    if (expediteur.equals(destinataire)) {
                        messagesBox.getChildren().add(conteneur);
                    }
                    // Sinon mettre en évidence dans la liste pour signaler un nouveau message
                    else {
                        onlineUsersList.refresh();
                        // TODO : ajouter une notification visuelle sur le nom de l'expéditeur
                    }
                });
            }

            @Override
            public void onUsersListReceived(List<String> connectes) {
                Platform.runLater(() -> {
                    onlineUsersList.getItems().clear();
                    for (String user : connectes) {
                        if (!user.equals(username)) {
                            onlineUsersList.getItems().add(user);
                        }
                    }
                });
            }

            @Override
            public void onOfflineUsersListReceived(List<String> offline) {
                Platform.runLater(() -> {
                    offlineUsersList.getItems().clear();
                    for (String user : offline) {
                        if (!user.equals(username)) {
                            offlineUsersList.getItems().add(user);
                        }
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                Platform.runLater(() -> afficherErreur("Connexion perdue avec le serveur !"));
            }
        });

        boolean connecte = client.connect(password);
        if (!connecte) {
            afficherErreur("Impossible de se connecter au serveur.");
        } else {
            Platform.runLater(() -> {
                headerLabel.setText("Connecté : " + username + " — sélectionne un utilisateur");
                headerLabel.getScene().getWindow().setOnCloseRequest(event -> {
                    if (client != null) client.disconnect();
                });
            });
        }
    }

    @FXML
    public void sendMessage() {
        if (destinataire == null) {
            afficherErreur("Sélectionne un utilisateur dans la liste !");
            return;
        }
        String texte = messageInput.getText();
        if (texte == null || texte.isEmpty()) return;

        afficherMessage(username + " :\n" + texte, true);

        Message message = new Message();
        message.setSender(username);
        message.setReceveur(destinataire);
        message.setContenue(texte);
        message.setDateEnvoie(new Date());
        message.setStatut(StatutMessage.ENVOYER);

        client.sendMessage(message);
        messageInput.clear();
    }

    private void afficherMessage(String texte, boolean estMoi) {
        HBox conteneur = new HBox();
        conteneur.setPadding(new Insets(4, 8, 4, 8));
        conteneur.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label label = new Label(texte);
        label.setWrapText(true);
        label.setMaxWidth(400);
        label.setPadding(new Insets(8, 12, 8, 12));

        if (estMoi) {
            label.setStyle("-fx-background-color: #375534; -fx-text-fill: #E3EED4; -fx-background-radius: 16 16 4 16; -fx-font-size: 13px;");
        } else {
            label.setStyle("-fx-background-color: #0F2A1D; -fx-text-fill: #E3EED4; -fx-background-radius: 16 16 16 4; -fx-font-size: 13px;");
        }

        conteneur.getChildren().add(label);
        messagesBox.getChildren().add(conteneur);
    }

    private void afficherErreur(String texte) {
        Label label = new Label(" " + texte);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        messagesBox.getChildren().add(label);
    }

    private void afficherInfo(String texte) {
        Label label = new Label(texte);
        label.setStyle("-fx-text-fill: #AEC3B0; -fx-font-size: 12px;");
        messagesBox.getChildren().add(label);
    }
}