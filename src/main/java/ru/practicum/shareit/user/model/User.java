package ru.practicum.shareit.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    @NotBlank(message = "имя не может быть пустым")
    private String name;
    @Column(name = "email")
    @Email(message = "некорректный email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
