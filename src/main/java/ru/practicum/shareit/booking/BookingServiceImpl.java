package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository,
                              BookingMapper bookingMapper,
                              ItemMapper itemMapper,
                              UserMapper userMapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Override
    public BookingDto create(Long bookerId, BookingDto bookingDto) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + bookerId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + bookingDto.getItemId() + " не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwnerId().equals(bookerId)) {
            throw new ValidationException("Нельзя забронировать свою вещь");
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }

        Booking booking = bookingMapper.toModel(bookingDto);
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);

        Booking created = bookingRepository.create(booking);
        return enrichBooking(bookingMapper.toDto(created));
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + booking.getItemId() + " не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Только владелец вещи может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Можно подтвердить только ожидающее бронирование");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return enrichBooking(bookingMapper.toDto(bookingRepository.update(booking)));
    }

    @Override
    public BookingDto findById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id " + booking.getItemId() + " не найдена"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Доступ к бронированию только для арендатора или владельца вещи");
        }

        return enrichBooking(bookingMapper.toDto(booking));
    }

    @Override
    public List<BookingDto> findByBookerId(Long bookerId, BookingState state) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + bookerId + " не найден"));

        return bookingRepository.findByBookerId(bookerId, state).stream()
                .map(booking -> enrichBooking(bookingMapper.toDto(booking)))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findByItemId(Long ownerId, Long itemId, BookingState state) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Только владелец может просматривать бронирования вещи");
        }

        return bookingRepository.findByItemId(itemId, state).stream()
                .map(booking -> enrichBooking(bookingMapper.toDto(booking)))
                .collect(Collectors.toList());
    }

    private BookingDto enrichBooking(BookingDto dto) {
        itemRepository.findById(dto.getItemId()).ifPresent(item -> dto.setItem(itemMapper.toDto(item)));
        userRepository.findById(dto.getBookerId()).ifPresent(user -> dto.setBooker(userMapper.toDto(user)));
        return dto;
    }
}