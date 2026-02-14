package ru.practicum.shareit.item.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.RequestCommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRepository.class})
public interface CommentMapper {

    @Mapping(target = "authorName", source = "authorId", qualifiedByName = "mapAuthorIdToName")
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "authorId", source = "authorId")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentDto commentDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itemId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "created", ignore = true)
    Comment toEntity(RequestCommentDto requestCommentDto);

    List<CommentDto> toDtoList(List<Comment> commentList);

    @Named("mapAuthorIdToName")
    default String mapAuthorIdToName(Long authorId) {
        return null;
    }
}