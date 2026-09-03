package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item create(Item item);

    Optional<Item> findById(Long id);

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByRequestId(Long requestId);

    List<Item> findAll();

    List<Item> search(String text);

    Item update(Item item);

    void deleteById(Long id);

    Comment createComment(Comment comment);

    List<Comment> findCommentsByItemId(Long itemId);
}