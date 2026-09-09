package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
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
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           ItemRequestRepository itemRequestRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository,
                           ItemMapper itemMapper,
                           CommentMapper commentMapper,
                           BookingMapper bookingMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemRequestRepository = itemRequestRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemMapper = itemMapper;
        this.commentMapper = commentMapper;
        this.bookingMapper = bookingMapper;
    }

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + ownerId + " не найден"));

        if (itemDto.getRequestId() != null) {
            itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException(
                            "Запрос с id " + itemDto.getRequestId() + " не найден"));
        }

        Item item = itemMapper.toModel(itemDto);
        item.setOwner(owner);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Редактировать вещь может только ее владелец");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id " + itemId + " не найдена"));

        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(commentRepository.findByItemIdOrderByIdAsc(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findTopByItemIdAndEndBeforeOrderByStartDesc(itemId, now)
                    .ifPresent(b -> dto.setLastBooking(bookingMapper.toShortDto(b)));
            bookingRepository.findTopByItemIdAndStartAfterOrderByStartAsc(itemId, now)
                    .ifPresent(b -> dto.setNextBooking(bookingMapper.toShortDto(b)));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findByOwnerId(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setComments(commentRepository.findByItemIdOrderByIdAsc(item.getId()).stream()
                            .map(commentMapper::toDto)
                            .collect(Collectors.toList()));
                    bookingRepository.findTopByItemIdAndEndBeforeOrderByStartDesc(item.getId(), now)
                            .ifPresent(b -> dto.setLastBooking(bookingMapper.toShortDto(b)));
                    bookingRepository.findTopByItemIdAndStartAfterOrderByStartAsc(item.getId(), now)
                            .ifPresent(b -> dto.setNextBooking(bookingMapper.toShortDto(b)));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id " + itemId + " не найдена"));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"));

        boolean hasCompletedBooking = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!hasCompletedBooking) {
            throw new BadRequestException(
                    "Оставить комментарий может только пользователь с завершённой арендой вещи");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentMapper.toDto(commentRepository.save(comment));
    }
}