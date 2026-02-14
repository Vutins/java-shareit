package ru.practicum.shareit.booking.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.RequestBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "item", ignore = true)
    BookingDto toDto(Booking booking);

    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "item", source = "item")
    Booking toEntity(BookingDto bookingDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booker", ignore = true)
    @Mapping(target = "status", ignore = true)
    Booking toEntity(RequestBookingDto requestBookingDto);

    List<BookingDto> toDtoList(List<Booking> bookings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookingFromDto(BookingDto bookingDto, @MappingTarget Booking booking);
    
    default Long mapUserDtoToLong(UserDto userDto) {
        return userDto != null ? userDto.getId() : null;
    }

    default Long mapItemDtoToLong(ItemDto itemDto) {
        return itemDto != null ? itemDto.getId() : null;
    }
}