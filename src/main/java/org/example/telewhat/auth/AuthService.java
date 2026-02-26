package org.example.telewhat.auth;

import jakarta.persistence.EntityManager;
import org.example.telewhat.entity.User;
import org.example.telewhat.enumeration.Off_on;
import org.example.telewhat.repository.UserRepository;
import org.postgresql.util.PasswordUtil;

public class AuthService {
    private UserRepository userRepository;

    public AuthService(EntityManager entityManager) {
        this.userRepository = new UserRepository(entityManager);
    }

    public boolean login(String username, String password) {

        User user = userRepository.findByUsername(username);

        if (user == null) {
            return false;
        }
        boolean valid = PasswordUtils.verifyPassword(password, user.getPassword());
        if (valid) {
            user.setStatus(Off_on.ONLINE);
            userRepository.update(user);
        }
        return valid;

    }
}
