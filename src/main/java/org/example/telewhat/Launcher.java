package org.example.telewhat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("PERSISTENCE");
        EntityManager em = emf.createEntityManager();

        System.out.println("✅ Hibernate connecté à la base !");
        Application.launch(HelloApplication.class, args);
    }
}
