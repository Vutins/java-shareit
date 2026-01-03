package ru.practicum.shareit.request.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {

    @NotNull
    private Long id;
    @NotBlank
    @NotNull
    private String description;
    @NotNull
    private User requestor;
    @NotNull
    private LocalDateTime created;
}
