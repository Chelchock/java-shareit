package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository,
                              BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    @Transactional
    public BookingDto create(Long bookerId, BookingDto bookingDto) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + bookerId + " не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        "Вещь с id " + bookingDto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(bookerId)) {
            throw new BadRequestException("Нельзя забронировать собственную вещь");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна для бронирования");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new BadRequestException("Даты начала и окончания обязательны");
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new BadRequestException("Дата начала должна быть раньше даты окончания");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!bookingDto.getEnd().isAfter(now)) {
            throw new BadRequestException("Даты бронирования должны быть в будущем");
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " +  bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Изменить статус можно только у бронирования в статусе WAITING");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        "Бронирование с id " + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException(
                    "Доступ к бронированию есть только у автора и владельца вещи");
        }
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getByBooker(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"));

        return bookingRepository.findByBookerIdOrderByStartDesc(userId).stream()
                .filter(booking -> matches(booking, state))
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getByOwner(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id " + userId + " не найден"));

        return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId).stream()
                .filter(booking -> matches(booking, state))
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private boolean matches(Booking booking, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                return booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case PAST:
                return booking.getEnd().isBefore(now);
            case FUTURE:
                return booking.getStart().isAfter(now);
            case WAITING:
                return booking.getStatus() == BookingStatus.WAITING;
            case REJECTED:
                return booking.getStatus() == BookingStatus.REJECTED;
            default:
                return true;
        }
    }
}