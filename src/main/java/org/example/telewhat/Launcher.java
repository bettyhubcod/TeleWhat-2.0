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

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PERSISTENCE");
        EntityManager em = emf.createEntityManager();

        Application.launch(HelloApplication.class, args);
        System.out.println("✅ Hibernate connecté à la base !");
        Application.launch(HelloApplication.class, args);
    }
}
