package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item create(Item item);

    Optional<Item> findById(Long id);

    List<Item> findByOwnerId(Long ownerId);

    List<Item> search(String text);

    Item update(Item item);

    void deleteById(Long id);
}
