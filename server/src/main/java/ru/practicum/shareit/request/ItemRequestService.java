package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(Long requesterId, ItemRequestDto requestDto);

    ItemRequestDto findById(Long userId, Long requestId);

    List<ItemRequestDto> findByRequesterId(Long requesterId);

    List<ItemRequestDto> findAll(Long userId, Integer from, Integer size);
}