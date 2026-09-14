package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @Valid @RequestBody BookItemRequestDto bookingDto
    ) {
        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/bookingId")
    public ResponseEntity<Object> approve(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @PathVariable @Positive Long bookingId,
            @RequestParam Boolean approved
    ) {
       return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @PathVariable @Positive Long bookingId
    ) {
        return bookingClient.findById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> findByBooker(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size
    ) {
        return bookingClient.findByBooker(
                userId,
                parseState(state),
                from,
                size
        );
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findByOwner(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size
    ) {
        return bookingClient.findByOwner(userId, parseState(state), from, size);
    }

    private BookingState parseState(String state) {
        return BookingState.from(state).orElseThrow(()->new IllegalArgumentException("Unknown state: " + state));
    }
}
