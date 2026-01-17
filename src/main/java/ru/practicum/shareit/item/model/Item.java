package ru.practicum.shareit.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("id")
    private Long id;
    @NotBlank
    @NotNull
    @JsonProperty("name")
    private String name;
    @NotBlank
    @NotNull
    @JsonProperty("description")
    private String description;
    @NotNull
    @JsonProperty("available")
    private boolean available;
    @NotNull
    @JsonProperty("owner")
    private User owner;
    @JsonProperty("request")
    private ItemRequest request;
}
