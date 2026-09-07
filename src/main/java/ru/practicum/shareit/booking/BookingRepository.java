package ru.practicum.shareit.booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking create(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findByBookerId(Long bookerId, BookingState state);

    List<Booking> findByItemId(Long itemId, BookingState state);

    Booking update(Booking booking);
}
