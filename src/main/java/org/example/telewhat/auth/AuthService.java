package org.example.telewhat.auth;

import org.example.telewhat.entity.User;
import jakarta.persistence.EntityManager;
import org.example.telewhat.enumeration.Status;
import org.example.telewhat.repository.UserRepository;
import jakarta.persistence.EntityTransaction;
import org.example.telewhat.utils.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    private UserRepository userRepository;
    private EntityManager em;

    public AuthService(EntityManager entityManager) {
        this.userRepository = new UserRepository(entityManager);
    }

    public AuthService() {
        em = JpaUtil.getEntityManager();
    }

    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }

        boolean valid = PasswordUtils.verifyPassword(password, user.getPassword());
        if (valid) {
            user.setStatut(Status.ONLINE);
            userRepository.update(user);
        }
        return valid;
    }

    public void registerUser(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);

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
}