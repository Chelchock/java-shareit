package ru.practicum.shareit.booking;

import org.springframework.expression.spel.ast.Literal;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long bookerId, BookingDto bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, Boolean approved);

    BookingDto findById(Long userId, Long bookingId);

    List<BookingDto> findByBookerId(Long bookerId, BookingState state);

    List<BookingDto> findByItemId(Long ownerId, Long itemId, BookingState state);
}
