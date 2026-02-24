package org.example.telewhat.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static boolean verifyPassword(String password, String hashedPassword) {
            return BCrypt.checkpw(password, hashedPassword);
    }
}
