package org.example.telewhat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.telewhat.client.Client;
import org.example.telewhat.entity.Message;
import org.example.telewhat.enumeration.StatutMessage;

import java.util.Date;
import java.util.List;

public class ChatController {

    @FXML private VBox messagesBox;
    @FXML private TextField messageInput;
    @FXML private ImageView backgroundImage;
    @FXML private ListView<String> onlineUsersList;
    @FXML private ListView<String> offlineUsersList;

    private Client client;
    private String username;
    private String destinataire;

    @FXML
    public void initialize() {
        Image img = new Image(getClass().getResourceAsStream("/images/bg-green.jpeg"));
        backgroundImage.setImage(img);

        // Quand on clique sur un user dans la liste → il devient le destinataire
        onlineUsersList.setOnMouseClicked(event -> {
            String selected = onlineUsersList.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.equals(username)) {
                destinataire = selected;
                messagesBox.getChildren().clear();
                afficherInfo("💬 Conversation avec " + destinataire);
            }
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        client = new Client(username, new Client.MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                Platform.runLater(() -> afficherMessage(
                        message.getSender() + " : \n" + message.getContenue(), false));
            }

            @Override
            public void onUsersListReceived(List<String> connectes) {
                Platform.runLater(() -> {
                    onlineUsersList.getItems().clear();
                    for (String user : connectes) {
                        if (!user.equals(username)) { // on n'affiche pas soi-même
                            onlineUsersList.getItems().add(user);
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

        afficherMessage(" " + texte, true);

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
            label.setStyle("-fx-background-color: #375534; -fx-text-fill: #E3EED4; " +
                    "-fx-background-radius: 16 16 4 16; -fx-font-size: 13px;");
        } else {
            label.setStyle("-fx-background-color: #0F2A1D; -fx-text-fill: #E3EED4; " +
                    "-fx-background-radius: 16 16 16 4; -fx-font-size: 13px;");
        }

        conteneur.getChildren().add(label);
        messagesBox.getChildren().add(conteneur);
    }

    private void afficherErreur(String texte) {
        Label label = new Label("⚠️ " + texte);
        label.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        messagesBox.getChildren().add(label);
    }

    private void afficherInfo(String texte) {
        Label label = new Label(texte);
        label.setStyle("-fx-text-fill: #AEC3B0; -fx-font-size: 12px;");
        messagesBox.getChildren().add(label);
    }
}