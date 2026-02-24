package org.example.telewhat.entity;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.example.telewhat.enumeration.StatutMessage;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;



    @Enumerated(EnumType.STRING)
    private StatutMessage statut;

    @NotNull
    private LocalDateTime dateCreation;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.statut = StatutMessage.LU;
        this.dateCreation = LocalDateTime.now();
    }
}