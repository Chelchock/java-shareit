package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

@Component
public class BookingMapper {
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingMapper(ItemMapper itemMapper, UserMapper userMapper) {
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    public BookingDto toDto(Booking booking) {
    BookingDto dto = new BookingDto();
    dto.setId(booking.getId());
    dto.setItemId(booking.getItem().getId());
    dto.setBookerId(booking.getBooker().getId());
    dto.setStart(booking.getStart());
    dto.setEnd(booking.getEnd());
    dto.setStatus(booking.getStatus());
    dto.setItem(itemMapper.toDto(booking.getItem()));
    dto.setBooker(userMapper.toDto(booking.getBooker()));
    return dto;
    }

    public BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStatus(booking.getStatus());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        return dto;
    }
}