package org.example.telewhat.auth;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.telewhat.utils.JPAUtils;

public class AuthController {
    private AuthService authService;
    private EntityManager entityManager;
    @FXML
    public void initialize() {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
        authService = new AuthService(entityManager);
    }

    @FXML
    private Button btn_login;

    @FXML
    private TextField idPassword;

    @FXML
    private TextField idUsername;

    @FXML
    void loginAction(ActionEvent event) {
        String username = idUsername.getText();
        String password = idPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            System.out.println("Tentative de login avec champs vides !");
            return;
        }

        boolean success = authService.login(username, password);

        if (success) {
            showAlert("Succès", "Connexion réussie !");
            System.out.println("Connexion réussie pour l'utilisateur : " + username);
            // Ici tu peux changer de scène, ouvrir le chat, etc.
        } else {
            showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect !");
            System.out.println("Échec de connexion pour l'utilisateur : " + username);
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