package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           ItemRequestRepository itemRequestRepository,
                           BookingRepository bookingRepository,
                           ItemMapper itemMapper,
                           CommentMapper commentMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.bookingRepository = bookingRepository;
        this.itemMapper = itemMapper;
        this.commentMapper = commentMapper;
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + ownerId + " не найден"));

        if (itemDto.getRequestId() != null) {
            itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Запрос с id " + itemDto.getRequestId() + " не найден"));
        }

        Item item = itemMapper.toModel(itemDto);
        item.setOwnerId(ownerId);

        return itemMapper.toDto(itemRepository.create(item));
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Редактировать вещь может только ее владелец");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toDto(itemRepository.update(item));
    }

    @Override
    public ItemDto findById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(itemRepository.findCommentsByItemId(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    @Override
    public List<ItemDto> findByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        String authorName = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"))
                .getName();

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompletedBooking = bookingRepository
                .findByBookerId(userId, BookingState.ALL).stream()
                .filter(booking -> booking.getItemId().equals(itemId))
                .anyMatch(booking -> booking.getStatus() == BookingStatus.APPROVED && booking.getEnd().isBefore(now));

        if (!hasCompletedBooking) {
            throw new ValidationException(
                    "Оставить комментарий может только пользователь с завершенной арендой вещи");
        }

        boolean alreadyCommented = itemRepository.findCommentsByItemId(itemId).stream()
                .anyMatch(comment -> comment.getAuthorId().equals(userId));

        if (alreadyCommented) {
            throw new ValidationException("Вы уже оставили комментарий к данной вещи");
        }

        Comment comment = commentMapper.toModel(commentDto);
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setAuthorName(authorName);
        comment.setCreated(now);

        return commentMapper.toDto(itemRepository.createComment(comment));
    }

    @Override
    public List<CommentDto> findCommentsByItemId(Long itemId) {
        itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        return itemRepository.findCommentsByItemId(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }
}
