package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Booking create(Booking booking) {
        booking.setId(idGenerator.incrementAndGet());
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookings.get(id));
    }

    @Override
    public List<Booking> findByBookerId(Long bookerId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        return bookings.values().stream()
                .filter(booking -> booking.getBookerId().equals(bookerId))
                .filter(booking -> filterByState(booking, state, now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByItemId(Long itemId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        return bookings.values().stream()
                .filter(booking -> booking.getItemId().equals(itemId))
                .filter(booking -> filterByState(booking, state, now))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Booking update(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    private boolean filterByState(Booking booking, BookingState state, LocalDateTime now) {
        switch (state) {
            case ALL:
                return true;
            case CURRENT:
                return booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case PAST:
                return booking.getEnd().isBefore(now);
            case FUTURE:
                return booking.getStart().isAfter(now);
            case WAITING:
                return booking.getStatus() == BookingStatus.WAITING;
            case REJECTED:
                return booking.getStatus() == BookingStatus.REJECTED;
            default:
                return false;
        }
    }
}