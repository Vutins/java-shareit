package ru.practicum.server.item.mapper;

import org.mapstruct.*;
import ru.practicum.dto.booking.BookingDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.item.ItemDto;
import ru.practicum.server.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    @Mapping(target = "request", source = "request")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    ItemDto toDto(Item item);

    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "request", source = "request")
    Item toEntity(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> items);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateItemFromDto(ItemDto itemDto, @MappingTarget Item item);

    @Named("toDtoWithDetails")
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "available", source = "item.available")
    @Mapping(target = "owner", source = "item.owner")
    @Mapping(target = "request", source = "item.request")
    @Mapping(target = "lastBooking", source = "lastBooking")
    @Mapping(target = "nextBooking", source = "nextBooking")
    @Mapping(target = "comments", source = "comments")
    ItemDto toDto(Item item, BookingDto lastBooking, BookingDto nextBooking, List<CommentDto> comments);
}