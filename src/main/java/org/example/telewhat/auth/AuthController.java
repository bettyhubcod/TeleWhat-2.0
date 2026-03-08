package org.example.telewhat.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JPAUtils;
import org.example.telewhat.utils.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import org.example.telewhat.ChatController;
public class AuthController {

    private AuthService authService;
    private EntityManager entityManager;

    @FXML private ImageView oeilIcon;

    @FXML private Label statusLabel;
    @FXML private TextField idUsername;
    @FXML private TextField IdPasswordVisible;
    @FXML private TextField IdUsername;
    @FXML private PasswordField IdPassword;
    @FXML private PasswordField idPassword;
    @FXML private TextField idPasswordVisible;
    @FXML private Button btn_login;
    @FXML private Button btn_register;


    @FXML
    public void initialize() {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
        authService = new AuthService(entityManager);

        // Pour inscription-view
        if (IdUsername != null) {
            ajouterEffetFocus(IdUsername,
                    "-fx-background-radius: 20px; -fx-background-color: #132019; -fx-border-color: white; -fx-border-radius: 20px; -fx-border-width: 0.2px; -fx-text-fill: white;"
            );
        }
        if (IdPassword != null) {
            ajouterEffetFocus(IdPassword,
                    "-fx-background-radius: 20px; -fx-background-color: #132019; -fx-border-color: white; -fx-border-radius: 20px; -fx-border-width: 0.2px;"
            );
        }

        // Pour login-view
        if (idUsername != null) {
            ajouterEffetFocus(idUsername,
                    "-fx-background-radius: 20px; -fx-background-color: #132019; -fx-border-color: white; -fx-border-radius: 20px; -fx-border-width: 0.2px; -fx-text-fill: white;"
            );
        }
        if (idPassword != null) {
            ajouterEffetFocus(idPassword,
                    "-fx-background-radius: 20px; -fx-background-color: #132019; -fx-border-color: white; -fx-border-radius: 20px; -fx-border-width: 0.2px;"
            );
        }

    }
    private boolean mdpVisible = false;
    @FXML
    void togglePassword(MouseEvent event) {
        // Détermine quelle vue est active
        PasswordField pwField = IdPassword != null ? IdPassword : idPassword;
        TextField pwVisible = IdPasswordVisible != null ? IdPasswordVisible : idPasswordVisible;

        if (pwField == null || pwVisible == null) return;

        if (mdpVisible) {
            pwField.setText(pwVisible.getText());
            pwField.setVisible(true);
            pwVisible.setVisible(false);
            if (oeilIcon != null) oeilIcon.setImage(new Image(getClass().getResourceAsStream("/images/oeil.png")));
            mdpVisible = false;
        } else {
            pwVisible.setText(pwField.getText());
            pwVisible.setVisible(true);
            pwField.setVisible(false);
            if (oeilIcon != null) oeilIcon.setImage(new Image(getClass().getResourceAsStream("/images/cacher.png")));
            mdpVisible = true;
        }
    }
    @FXML
    void allerInscription(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/telewhat/inscription-view.fxml"));
        Scene scene = new Scene(loader.load(), 899, 463);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    @FXML
    void allerLogin(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/telewhat/login-view.fxml"));
        Scene scene = new Scene(loader.load(), 899, 463);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
    private void ajouterEffetFocus(TextInputControl field, String styleDefaut) {
        field.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (isFocused) {
                field.setStyle(
                        "-fx-background-radius: 20px;" +
                                "-fx-background-color: #132019;" +
                                "-fx-border-color: #367d53;" +
                                "-fx-border-radius: 20px;" +
                                "-fx-border-width: 2px;" +
                                "-fx-text-fill: white;"
                );
            } else {
                field.setStyle(styleDefaut);
            }
        });
    }
    void changement_border(TextInputControl field) {
        field.setStyle(
                "-fx-background-radius: 20px;" +
                        "-fx-background-color: #132019;" +
                        "-fx-border-color: #367d53;" +
                        "-fx-border-radius: 20px;" +
                        "-fx-border-width: 2px;" +
                        "-fx-text-fill: white;"
        );
    }
    @FXML
    void loginAction(ActionEvent event) {
        String username = idUsername.getText();
        String password = mdpVisible ? idPasswordVisible.getText() : idPassword.getText();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs !");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            return;
        }

        boolean success = authService.login(username, password);
        if (success) {
            statusLabel.setText("Connexion réussie !");
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/telewhat/chat-view.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);

                ChatController chatController = loader.getController();
                chatController.setUsername(username);

                // Tester la connexion au serveur AVANT d'ouvrir la fenêtre
                boolean connecte = chatController.testerConnexion(password);
                if (!connecte) {
                    statusLabel.setText("Utilisateur deja connecte ou serveur innaccessible !");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
                    return;
                }

                Stage chatStage = new Stage();
                chatStage.setTitle("TeleWhat - " + username);
                chatStage.setScene(scene);
                chatStage.setResizable(false);
                chatStage.show();

                idUsername.clear();
                idPassword.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Nom d'utilisateur ou mot de passe incorrect !");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        }
    }

    @FXML
    private void btn_inscription(ActionEvent event) {
        String username = IdUsername.getText().trim();
        String password = mdpVisible ? IdPasswordVisible.getText() : IdPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs !");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User(username, hashedPassword);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
            statusLabel.setText("Inscription réussie !");
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");


            IdUsername.clear();
            IdPassword.clear();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            statusLabel.setText("Nom d'utilisateur déjà utilisé !");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px; ");
        } finally {
            em.close();
        }
    }
}