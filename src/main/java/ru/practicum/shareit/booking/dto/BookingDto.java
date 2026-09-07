package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull(message = "ID вещи должен быть указан")
    private Long itemId;

    private Long bookerId;

    @NotNull(message = "Дата начала должна быть указана")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания должна быть указана")
    private LocalDateTime end;

    private BookingStatus status;
    private ItemDto item;
    private UserDto booker;
}