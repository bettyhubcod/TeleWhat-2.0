package org.example.telewhat.auth;

import org.example.telewhat.entity.User; // ton entity User
import jakarta.persistence.EntityManager;
import org.example.telewhat.enumeration.Off_on;
import org.example.telewhat.repository.UserRepository;
import org.postgresql.util.PasswordUtil;
import jakarta.persistence.EntityTransaction;
import org.example.telewhat.utils.JpaUtil; // ton helper pour EntityManager
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private UserRepository userRepository;

    public AuthService(EntityManager entityManager) {
        this.userRepository = new UserRepository(entityManager);
    private EntityManager em;

    public AuthService() {
        em = JpaUtil.getEntityManager(); // obtenir un EntityManager
    }

    public boolean login(String username, String password) {
    // Inscription : créer un nouvel utilisateur
    public void registerUser(String username, String password) {
        // Hash du mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = userRepository.findByUsername(username);
        // Création de l'entité User
        User user = new User();
        user.setUsername(username);          // <-- utiliser username
        user.setPassword(hashedPassword);

        if (user == null) {
            return false;
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
        boolean valid = PasswordUtils.verifyPassword(password, user.getPassword());
        if (valid) {
            user.setStatus(Off_on.ONLINE);
            userRepository.update(user);
        }
        return valid;
    }

    }
}