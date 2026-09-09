package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  ItemRepository itemRepository,
                                  UserRepository userRepository,
                                  ItemRequestMapper itemRequestMapper,
                                  ItemMapper itemMapper) {
        this.itemRequestRepository = itemRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemRequestMapper = itemRequestMapper;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional
    public ItemRequestDto create(Long requesterId, ItemRequestDto requestDto) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + requesterId + " не найден"));

        ItemRequest request = itemRequestMapper.toModel(requestDto);
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        return itemRequestMapper.toDtoWithItems(itemRequestRepository.save(request), List.of());
    }

    @Override
    @Transactional
    public ItemRequestDto findById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<ItemDto> items = itemRepository.findByRequestIdOrderByIdAsc(requestId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());

        return itemRequestMapper.toDtoWithItems(request, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> findByRequesterId(Long requesterId) {
        userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + requesterId + " не найден"));

        return enrichWithItems(
                itemRequestRepository.findByRequesterIdOrderByCreatedDesc(requesterId));
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<ItemRequest> all = itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(userId);

        int fromIndex = Math.min(from, all.size());
        int toIndex = Math.min(from + size, all.size());
        return enrichWithItems(all.subList(fromIndex, toIndex));
    }

    private List<ItemRequestDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        Set<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toSet());

        Map<Long, List<ItemDto>> itemsByRequestId = itemRepository.findAll()
                .stream()
                .filter(item -> item.getRequestId() != null && requestIds.contains(item.getRequestId()))
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(itemMapper::toDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> itemRequestMapper.toDtoWithItems(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }
}