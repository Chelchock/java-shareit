package ru.practicum.shareit.request;

import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryRequestRepository implements ItemRequestRepository {
    private final Map<Long, ItemRequest> requests = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public ItemRequest create(ItemRequest request) {
        request.setId(idGenerator.incrementAndGet());
        requests.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<ItemRequest> findById(Long id) {
        return Optional.ofNullable(requests.get(id));
    }

    @Override
    public List<ItemRequest> findByRequesterId(Long requesterId) {
        return requests.values().stream()
                .filter(request -> request.getRequesterId().equals(requesterId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequest> findAllExceptRequester(Long requesterId) {
        return requests.values().stream()
                .filter(request -> !request.getRequesterId().equals(requesterId))
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());
    }
}
