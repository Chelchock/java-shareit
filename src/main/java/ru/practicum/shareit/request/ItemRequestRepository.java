package ru.practicum.shareit.request;

import java.util.List;
import java.util.Optional;

public interface ItemRequestRepository {
    ItemRequest create(ItemRequest request);

    Optional<ItemRequest> findById(Long id);

    List<ItemRequest> findByRequesterId(Long requesterId);

    List<ItemRequest> findAllExceptRequester(Long requesterId);
}