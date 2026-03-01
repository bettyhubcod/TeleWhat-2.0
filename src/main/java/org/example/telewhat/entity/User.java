package org.example.telewhat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import org.example.telewhat.enumeration.Status;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status statut = Status.OFFLINE;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.statut = Status.OFFLINE;
        this.dateCreation = LocalDateTime.now();
    }
}