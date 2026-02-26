package org.example.telewhat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;
import org.example.telewhat.entity.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;

public class Launcher {
    public static void main(String[] args) {

        String hashed = BCrypt.hashpw("betty", BCrypt.gensalt());
        System.out.println(hashed);

        Application.launch(HelloApplication.class, args);
    }
}
