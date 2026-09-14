package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.OnCreate;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                         @Validated(OnCreate.class) @RequestBody ItemDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody ItemDto itemDto) {
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(USER_ID_HEADER) @Positive long userId,
                                           @PathVariable @Positive Long itemId) {
        return itemClient.findById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findByOwner(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size
    ) {
        return itemClient.findByOwner(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search (
            @RequestParam String text,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10")
            @Positive Integer size
    ) {
        return itemClient.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader(USER_ID_HEADER) @Positive long userId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
