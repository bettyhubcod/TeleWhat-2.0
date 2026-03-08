package org.example.telewhat.service;

import org.example.telewhat.entity.Message;
import org.example.telewhat.enumeration.StatutMessage;
import org.example.telewhat.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageService {

    public void sauvegarder(Message message) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (message.getDateEnvoie() == null) message.setDateEnvoie(new Date());
            if (message.getStatut() == null) message.setStatut(StatutMessage.ENVOYER);
            em.persist(message);
            tx.commit();
            System.out.println("💾 Message sauvegardé : " + message.getSender() + " → " + message.getReceveur());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("❌ Erreur sauvegarde message : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<Message> getHistorique(String user1, String user2) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE (m.sender = :user1 AND m.receveur = :user2) " +
                                    "OR (m.sender = :user2 AND m.receveur = :user1) " +
                                    "ORDER BY m.dateEnvoie ASC", Message.class)
                    .setParameter("user1", user1)
                    .setParameter("user2", user2)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("❌ Erreur récupération historique : " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Message> getMessagesEnAttente(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT m FROM Message m " +
                                    "WHERE m.receveur = :username " +
                                    "AND m.statut = :statut " +
                                    "ORDER BY m.dateEnvoie ASC", Message.class)
                    .setParameter("username", username)
                    .setParameter("statut", StatutMessage.ENVOYER)
                    .getResultList();
        } catch (Exception e) {
            System.out.println("❌ Erreur récupération messages en attente : " + e.getMessage());
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public void mettreAJourStatut(Long messageId, StatutMessage nouveauStatut) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                            "UPDATE Message m SET m.statut = :statut WHERE m.id = :id")
                    .setParameter("statut", nouveauStatut)
                    .setParameter("id", messageId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("❌ Erreur mise à jour statut message : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // =========================================
    // MARQUER COMME LUS (LU)
    // =========================================

    /**
     * Marque tous les messages de sender → receveur comme LU
     * Appelé quand receveur ouvre la conversation
     */
    public void marquerCommeLus(String sender, String receveur) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                            "UPDATE Message m SET m.statut = :statut " +
                                    "WHERE m.sender = :sender AND m.receveur = :receveur " +
                                    "AND m.statut = :ancienStatut")
                    .setParameter("statut", StatutMessage.LU)
                    .setParameter("sender", sender)
                    .setParameter("receveur", receveur)
                    .setParameter("ancienStatut", StatutMessage.RECU)
                    .executeUpdate();
            tx.commit();
            System.out.println("👁️ Messages de " + sender + " → " + receveur + " marqués comme LU");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("❌ Erreur marquer comme lus : " + e.getMessage());
        } finally {
            em.close();
        }
    }
}