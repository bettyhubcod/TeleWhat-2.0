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

        String password = "monMotDePasse"; // mot de passe clair

        Application.launch(HelloApplication.class, args);
    }
}
