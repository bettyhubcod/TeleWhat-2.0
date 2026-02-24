package org.example.telewhat.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JPAUtils;

public class UserRepository {
    private EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
    }

    public User findByUsername(String username) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void update(User user) {
        entityManager.getTransaction().begin();
        entityManager.merge(user);  // merge met à jour un objet existant
        entityManager.getTransaction().commit();
    }
}
