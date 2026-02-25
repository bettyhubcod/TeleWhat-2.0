package org.example.telewhat.auth;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class AuthController {

    // ---------- FXML ----------

    @FXML
    private TextField IdUsername;

    @FXML
    private TextField IdPassword;

    @FXML
    private Button btn_register;

    @FXML
    private Label statusLabel;

    // ---------- Méthodes ----------

    /**
     * Méthode appelée lorsque l'utilisateur clique sur le bouton "S'inscrire"
     */
    @FXML
    private void btn_inscription(ActionEvent event) {
        String username = IdUsername.getText().trim();
        String password = IdPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            System.out.println("Tentative d'inscription avec champs vides !");
            return;
        }

        // Hash du mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Création de l'entité User
        User user = new User(username, hashedPassword);

        // Persistance JPA
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