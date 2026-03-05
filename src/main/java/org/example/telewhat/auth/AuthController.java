package org.example.telewhat.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JPAUtils;
import org.example.telewhat.utils.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import org.example.telewhat.ChatController;
public class AuthController {

    private AuthService authService;
    private EntityManager entityManager;

    @FXML private TextField idUsername;
    @FXML private TextField idPassword;
    @FXML private TextField IdUsername;
    @FXML private TextField IdPassword;
    @FXML private Button btn_login;
    @FXML private Button btn_register;
    @FXML private Label statusLabel;
    @FXML
    public void initialize() {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
        authService = new AuthService(entityManager);
    }

    @FXML
    void loginAction(ActionEvent event) {
        String username = idUsername.getText();
        String password = idPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            return;
        }

        boolean success = authService.login(username, password);
        if (success) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/telewhat/chat-view.fxml"));
                Scene scene = new Scene(loader.load(), 900, 600);

                // Passer le username au ChatController
                ChatController chatController = loader.getController();
                chatController.setUsername(username);
                chatController.setPassword(password);

                Stage stage = (Stage) btn_login.getScene().getWindow();
                stage.setTitle("TeleWhat - " + username);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect !");
        }
    }

    @FXML
    private void btn_inscription(ActionEvent event) {
        String username = IdUsername.getText().trim();
        String password = IdPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            System.out.println("Tentative d'inscription avec champs vides !");
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
            showAlert("Succès", "Inscription réussie !");
            System.out.println("Inscription réussie pour l'utilisateur : " + username);
            IdUsername.clear();
            IdPassword.clear();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            showAlert("Erreur", "Nom d'utilisateur deja utilisé !");
            System.out.println("Échec d'inscription pour l'utilisateur : " + username);
        } finally {
            em.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}