package ru.practicum.shareit.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;

    @NotNull(message = "Id вещи должен быть указан")
    private Long itemId;

    private Long bookerId;

    @NotNull(message = "Дата начала должна быть указана")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания должна быть указана")
    private LocalDateTime end;

    @NotNull(message = "Статус должен быть указан")
    private BookingStatus status;
}
