package org.example.telewhat.auth;

import org.example.telewhat.entity.User; // ton entity User
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.telewhat.utils.JpaUtil; // ton helper pour EntityManager
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private EntityManager em;

    public AuthService() {
        em = JpaUtil.getEntityManager(); // obtenir un EntityManager
    }

    // Inscription : créer un nouvel utilisateur
    public void registerUser(String username, String password) {
        // Hash du mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Création de l'entité User
        User user = new User();
        user.setUsername(username);          // <-- utiliser username
        user.setPassword(hashedPassword);

        // Persister en base
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    // Connexion : vérifier l'utilisateur
    public boolean loginUser(String username, String password) {
        User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)   // <-- utiliser username
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (user == null) return false;

        return BCrypt.checkpw(password, user.getPassword());
    }
}