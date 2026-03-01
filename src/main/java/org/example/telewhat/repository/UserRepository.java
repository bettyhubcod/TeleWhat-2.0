package org.example.telewhat.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.EntityTransaction;
import org.example.telewhat.entity.User;
import org.example.telewhat.utils.JPAUtils;
import org.example.telewhat.utils.JpaUtil;

import java.util.List;

public class UserRepository {
    private EntityManager entityManager;

    public UserRepository(EntityManager entityManager) {
        this.entityManager = JPAUtils.getEntityManagerFactory().createEntityManager();
    public UserRepository() {
        this.entityManager = JpaUtil.getEntityManager();
    }

    public User findByUsername(String username) {
    // ✅ Sauvegarder un utilisateur
    public void save(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
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
