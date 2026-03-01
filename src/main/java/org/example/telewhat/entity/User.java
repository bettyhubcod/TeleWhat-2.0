package org.example.telewhat.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.example.telewhat.enumeration.Status;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "users")
public class User  implements Serializable  {
    private static final long serialVersionUID = 2L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // 👈 IMPORTANT
    @Column(nullable = false)
    private Status statut = Status.OFFLINE; // 👈 par défaut

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.statut = Status.OFFLINE; // 👈 OFFLINE par défaut
        this.dateCreation = LocalDateTime.now();
    }
}
