package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) Long requesterId,
                                 @Valid @RequestBody ItemRequestDto requestDto) {
        return itemRequestService.create(requesterId, requestDto);
    }

    @GetMapping
    public List<ItemRequestDto> findByRequesterId(@RequestHeader(USER_ID_HEADER) Long requesterId) {
        return itemRequestService.findByRequesterId(requesterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAll(@RequestHeader(USER_ID_HEADER) Long userId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@RequestHeader(USER_ID_HEADER) Long userId,
                                   @PathVariable Long requestId) {
        return itemRequestService.findById(userId, requestId);
    }
}