package ru.practicum.server.request.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.item.ItemResponseDto;
import ru.practicum.dto.request.ItemRequestDto;
import ru.practicum.server.request.model.ItemRequest;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ItemRequestMapper {

    ItemRequestDto toItemRequestDto(ItemRequest itemRequest);

    @Mapping(target = "requestor", ignore = true)
    ItemRequest toItemRequest(ItemRequestDto itemRequestDto);

    default ItemRequestDto toItemRequestDtoWithItems(ItemRequest itemRequest, List<ItemResponseDto> items) {
        ItemRequestDto dto = toItemRequestDto(itemRequest);
        dto.setItems(items);
        return dto;
    }
}