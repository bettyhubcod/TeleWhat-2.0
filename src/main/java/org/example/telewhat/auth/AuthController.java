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
import javafx.scene.control.*;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.persistence.EntityTransaction;

public class AuthController {
    private AuthService authService;
    private EntityManager entityManager;

    // ---------- FXML ----------

    @FXML
    public void initialize() {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
        authService = new AuthService(entityManager);
    }
    private TextField IdUsername;

    @FXML
    private Button btn_login;
    private TextField IdPassword;

    @FXML
    private TextField idPassword;
    private Button btn_register;

    @FXML
    private TextField idUsername;
    private Label statusLabel;

    @FXML
    void loginAction(ActionEvent event) {
        String username = idUsername.getText();
        String password = idPassword.getText();
    private void btn_inscription(ActionEvent event) {
        String username = IdUsername.getText().trim();
        String password = IdPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs !");
            System.out.println("Tentative de login avec champs vides !");
            System.out.println("Tentative d'inscription avec champs vides !");
            return;
        }

        boolean success = authService.login(username, password);
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

        if (success) {
            showAlert("Succès", "Connexion réussie !");
            System.out.println("Connexion réussie pour l'utilisateur : " + username);
            // Ici tu peux changer de scène, ouvrir le chat, etc.
        } else {
            showAlert("Erreur", "Nom d'utilisateur ou mot de passe incorrect !");
            System.out.println("Échec de connexion pour l'utilisateur : " + username);
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