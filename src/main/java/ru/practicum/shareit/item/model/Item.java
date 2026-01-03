package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    @NotNull
    private Long id;
    @NotBlank
    @NotNull
    private String name;
    @NotBlank
    @NotNull
    private String description;
    @NotNull
    private boolean available;
    @NotNull
    private User owner;
    private ItemRequest request;
}
