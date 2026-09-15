package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        return requestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findOwn(
            @RequestHeader(USER_ID_HEADER) @Positive long userId)
    {
        return requestClient.findOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                          @RequestParam(defaultValue = "0")
                                          @PositiveOrZero Integer from,
                                          @RequestParam(defaultValue = "10")
                                          @Positive Integer size) {
        return requestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @RequestHeader(value = USER_ID_HEADER) @Positive long userId,
            @PathVariable @Positive Long requestId
    ) {
        return requestClient.findById(userId, requestId);
    }
}