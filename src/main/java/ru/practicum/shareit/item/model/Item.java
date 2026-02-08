package ru.practicum.shareit.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
public class Item {

    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private Long id;
    @NotBlank
    @NotNull
    @JsonProperty("name")
    @Column(name = "name")
    private String name;
    @NotBlank
    @NotNull
    @JsonProperty("description")
    @Column(name = "description")
    private String description;
    @NotNull
    @JsonProperty("available")
    @Column(name = "available")
    private Boolean available;
    @NotNull
    @JsonProperty("owner")
    @Column(name = "owner_id")
    private Long owner;
    @JsonProperty("request")
    @Column(name = "request_id")
    private Long request;
}
