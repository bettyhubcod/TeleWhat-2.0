package org.example.telewhat.service;

import org.example.telewhat.entity.User;
import org.example.telewhat.repository.UserRepository;
import org.example.telewhat.utils.JpaUtil;
public class UserService {

    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }
}