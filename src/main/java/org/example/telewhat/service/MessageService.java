package org.example.telewhat.service;

import org.example.telewhat.entity.Message;
import org.example.telewhat.enumeration.StatutMessage;
import org.example.telewhat.utils.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Service gérant toutes les opérations liées aux messages
 * Sauvegarde, historique, messages en attente
 */
public class MessageService {

    // =========================================
    // SAUVEGARDER UN MESSAGE (RG6)
    // =========================================

    /**
     * Sauvegarde un message en base
     * Appelé par ClientHandler quand le destinataire est hors ligne (RG6)
     */
    public void sauvegarder(Message message) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // S'assurer que la date et le statut sont définis
            if (message.getDateEnvoie() == null) {
                message.setDateEnvoie(new Date());
            }
            if (message.getStatut() == null) {
                message.setStatut(StatutMessage.ENVOYER);
            }

            em.persist(message);
            tx.commit();

            System.out.println("💾 Message sauvegardé : "
                    + message.getSender() + " → " + message.getReceveur());

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("❌ Erreur sauvegarde message : " + e.getMessage());
        } finally {
            em.close();
        }
    }

    // =========================================
    // HISTORIQUE (RG8)
    // =========================================

    /**
     * Récupère l'historique des messages entre deux utilisateurs
     * RG8 : triés par ordre chronologique
     */
    public List<Message> getHistorique(String user1, String user2) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            // Récupère les messages dans les deux sens :
            // user1 → user2 ET user2 → user1
            // Triés par dateEnvoie (RG8)
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
            return Collections.emptyList(); // retourne liste vide si erreur
        } finally {
            em.close();
        }
    }

    // =========================================
    // MESSAGES EN ATTENTE (RG6)
    // =========================================

    /**
     * Récupère les messages reçus pendant que l'utilisateur était hors ligne
     * RG6 : livrés à la prochaine connexion
     */
    public List<Message> getMessagesEnAttente(String username) {
        EntityManager em = JpaUtil.getEntityManager();

        try {
            // Cherche tous les messages ENVOYE destinés à cet utilisateur
            // ENVOYE = envoyé mais pas encore livré
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

    // =========================================
    // METTRE À JOUR LE STATUT D'UN MESSAGE
    // =========================================

    /**
     * Met à jour le statut d'un message (ENVOYE → RECU → LU)
     */
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
}