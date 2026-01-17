package ru.practicum.shareit.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;
    @NotBlank(message = "имя не может быть пустым")
    private String name;
    @Email(message = "некорректный email")
    @NotBlank(message = "email не может быть пустым")
    private String email;
}
