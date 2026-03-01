package org.example.telewhat.entity;
import jakarta.persistence.*;
import lombok.*;
import org.example.telewhat.enumeration.Off_on;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.time.LocalDateTime;
import org.example.telewhat.enumeration.Status;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Off_on status;
    @NotNull
    private Date dateCreation;

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