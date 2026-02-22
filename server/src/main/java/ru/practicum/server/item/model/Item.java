package ru.practicum.server.item.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.server.item.comment.model.Comment;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {

    @Id
    @JsonProperty("id")
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotBlank
    @NotNull
    @JsonProperty("name")
    @Column(name = "name")
    String name;
    @NotBlank
    @NotNull
    @JsonProperty("description")
    @Column(name = "description")
    String description;
    @NotNull
    @JsonProperty("available")
    @Column(name = "available")
    Boolean available;
    @NotNull
    @JsonProperty("owner")
    @Column(name = "owner_id")
    Long owner;
    @JsonProperty("request")
    @Column(name = "request_id")
    Long request;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private List<Comment> comments = new ArrayList<>();
}
