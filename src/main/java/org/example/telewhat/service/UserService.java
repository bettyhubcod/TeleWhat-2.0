package org.example.telewhat.service;

import org.example.telewhat.entity.User;
import org.example.telewhat.enumeration.Status;
import org.example.telewhat.repository.UserRepository;

import java.util.List;

public class UserService {

    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    // RG4 : passer OFFLINE à la déconnexion
    public void deconnecter(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setStatut(Status.OFFLINE);
            userRepository.update(user);
            System.out.println("[UserService] " + username + " est maintenant OFFLINE.");
        }
    }

    // RG5 : vérifier qu'un utilisateur existe
    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }

    // RG5 : vérifier qu'un utilisateur est connecté
    public boolean isOnline(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getStatut() == Status.ONLINE;
    }

public List<String> getTousLesUsers() {
    return userRepository.findAllUsernames();
}




}