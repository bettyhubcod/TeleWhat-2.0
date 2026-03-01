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