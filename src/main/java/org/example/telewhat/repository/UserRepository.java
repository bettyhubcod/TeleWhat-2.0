package org.example.telewhat.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JpaUtil;

import java.util.List;

public class UserRepository {

    private EntityManager entityManager;

    public UserRepository() {
        this.entityManager = JpaUtil.getEntityManager();
    }

    // ✅ Sauvegarder un utilisateur
    public void save(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }


}
