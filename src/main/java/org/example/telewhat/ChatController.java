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
import javafx.stage.FileChooser;
import org.example.telewhat.client.Client;
import org.example.telewhat.entity.Message;
import org.example.telewhat.enumeration.StatutMessage;
import org.example.telewhat.service.MessageService;

import java.io.File;
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
                    setText(" " + item);
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
                    setText("   " + item);
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

        messageService.marquerCommeLus(destinataire, username);

        if (client != null) {
            client.envoyerLectureNotification(destinataire);
        }

        List<Message> historique = messageService.getHistorique(username, destinataire);
        for (Message msg : historique) {
            boolean estMoi = msg.getSender().equals(username);
            afficherMessageAvecStatut(msg, estMoi);
        }

        discussions.put(destinataire, new ArrayList<>(messagesBox.getChildren()));
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean testerConnexion(String password) {
        client = new Client(username, new Client.MessageListener() {
            @Override
            public void onMessageReceived(Message message) {
                Platform.runLater(() -> {
                    String expediteur = message.getSender();
                    if (expediteur.equals(destinataire)) {
                        afficherMessageAvecStatut(message, false);
                        messageService.marquerCommeLus(expediteur, username);
                        client.envoyerLectureNotification(expediteur);
                    } else {
                        discussions.computeIfAbsent(expediteur, k -> new ArrayList<>());
                        onlineUsersList.refresh();
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
            public void onLectureNotificationReceived(String reader) {
                Platform.runLater(() -> {
                    if (reader.equals(destinataire)) {
                        List<Message> historique = messageService.getHistorique(username, destinataire);
                        messagesBox.getChildren().clear();
                        for (Message msg : historique) {
                            boolean estMoi = msg.getSender().equals(username);
                            afficherMessageAvecStatut(msg, estMoi);
                        }
                        discussions.put(destinataire, new ArrayList<>(messagesBox.getChildren()));
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                Platform.runLater(() -> afficherErreur("Connexion perdue avec le serveur !"));
            }
        });

        boolean connecte = client.connect(password);
        if (connecte) {
            Platform.runLater(() -> {
                if (headerLabel.getScene() != null) {
                    headerLabel.setText("Connecté : " + username + " — sélectionne un utilisateur");
                    headerLabel.getScene().getWindow().setOnCloseRequest(e -> {
                        if (client != null) client.disconnect();
                    });
                }
            });
        }
        return connecte;
    }

    @FXML
    public void sendMessage() {
        if (destinataire == null) {
            afficherErreur("Sélectionne un utilisateur dans la liste !");
            return;
        }
        String texte = messageInput.getText();
        if (texte == null || texte.isEmpty()) return;

        Message message = new Message();
        message.setSender(username);
        message.setReceveur(destinataire);
        message.setContenue(texte);
        message.setDateEnvoie(new Date());
        message.setStatut(StatutMessage.ENVOYER);

        client.sendMessage(message);
        afficherMessageAvecStatut(message, true);
        messageInput.clear();
    }

    @FXML
    public void choisirPhoto() {
        if (destinataire == null) {
            afficherErreur("Sélectionne un utilisateur d'abord !");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File fichier = fileChooser.showOpenDialog(messageInput.getScene().getWindow());
        if (fichier != null) {
            if (fichier.length() > 5 * 1024 * 1024) {
                afficherErreur("Image trop grande ! Max 5MB.");
                return;
            }

            String cheminImage = "IMG:" + fichier.toURI().toString();

            Message message = new Message();
            message.setSender(username);
            message.setReceveur(destinataire);
            message.setContenue(cheminImage);
            message.setDateEnvoie(new Date());
            message.setStatut(StatutMessage.ENVOYER);

            client.sendMessage(message);
            afficherMessageAvecStatut(message, true);
        }
    }

    private void afficherMessageAvecStatut(Message message, boolean estMoi) {
        HBox conteneur = new HBox();
        conteneur.setPadding(new Insets(4, 8, 4, 8));
        conteneur.setAlignment(estMoi ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bulle = new VBox(0);
        bulle.setMaxWidth(400);

        if (estMoi) {
            bulle.setStyle("-fx-background-color: #375534; -fx-background-radius: 16 16 4 16;");
        } else {
            bulle.setStyle("-fx-background-color: #0F2A1D; -fx-background-radius: 16 16 16 4;");
        }

        // Détecter si c'est une image
        boolean estImage = message.getContenue() != null
                && message.getContenue().startsWith("IMG:");

        if (estImage) {
            String uri = message.getContenue().substring(4);
            try {
                Image img = new Image(uri, 250, 200, true, true);
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(250);
                imgView.setPreserveRatio(true);
                HBox imgBox = new HBox(imgView);
                imgBox.setPadding(new Insets(6, 6, 2, 6));
                bulle.getChildren().add(imgBox);
            } catch (Exception e) {
                Label err = new Label("🖼 Image non disponible");
                err.setStyle("-fx-text-fill: #AEC3B0; -fx-padding: 8 12 8 12;");
                bulle.getChildren().add(err);
            }
        } else {
            Label labelTexte = new Label(message.getSender() + " :\n" + message.getContenue());
            labelTexte.setWrapText(true);
            labelTexte.setMaxWidth(380);
            labelTexte.setPadding(new Insets(8, 12, 2, 12));
            labelTexte.setStyle("-fx-background-color: transparent; -fx-text-fill: #E3EED4; -fx-font-size: 13px;");
            bulle.getChildren().add(labelTexte);
        }

        // Statut ✓ uniquement pour mes messages
        if (estMoi) {
            HBox statutBox = new HBox();
            statutBox.setAlignment(Pos.CENTER_RIGHT);
            statutBox.setPadding(new Insets(0, 8, 4, 0));

            Label labelStatut = new Label();
            switch (message.getStatut()) {
                case ENVOYER -> {
                    labelStatut.setText("\u2713");
                    labelStatut.setStyle("-fx-text-fill: #AEC3B0; -fx-font-size: 12px; -fx-font-weight: bold;");
                }
                case RECU -> {
                    labelStatut.setText("\u2713\u2713");
                    labelStatut.setStyle("-fx-text-fill: #AEC3B0; -fx-font-size: 12px; -fx-font-weight: bold;");
                }
                case LU -> {
                    labelStatut.setText("\u2713\u2713");
                    labelStatut.setStyle("-fx-text-fill: #4FC3F7; -fx-font-size: 12px; -fx-font-weight: bold;");
                }
            }

            statutBox.getChildren().add(labelStatut);
            bulle.getChildren().add(statutBox);
        }

        conteneur.getChildren().add(bulle);
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