package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    Optional<Booking>
    findTopByItemIdAndStatusAndEndBeforeOrderByStartDesc(
            Long itemId,
            BookingStatus status,
            LocalDateTime time
    );

    Optional<Booking>
    findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime time
    );

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId,
            Long itemId,
            BookingStatus status,
            LocalDateTime time
    );
}