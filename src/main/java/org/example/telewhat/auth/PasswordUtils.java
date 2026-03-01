package org.example.telewhat.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // Hash un mot de passe
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
            return BCrypt.checkpw(password, hashedPassword);
    // Vérifier un mot de passe
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
