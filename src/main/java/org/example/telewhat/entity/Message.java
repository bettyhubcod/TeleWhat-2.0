package org.example.telewhat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.telewhat.enumeration.StatutMessage;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String sender;

    @NotBlank
    private String receveur;

    @NotBlank
    @Size(max = 1000)
    private String contenue;

    @NotNull
    private Date dateEnvoie;

    @NotNull
    @Enumerated(EnumType.STRING)
    private StatutMessage statut;

}
