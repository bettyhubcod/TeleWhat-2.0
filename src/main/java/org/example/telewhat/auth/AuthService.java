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
        this.userRepository = new UserRepository(em);
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


}