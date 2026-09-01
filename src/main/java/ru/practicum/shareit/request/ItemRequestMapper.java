package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequestMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemRequestDto toDto(ItemRequest request) {
        if (request == null) {
            return null;
        }
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setRequesterId(request.getRequesterId());
        dto.setCreated(request.getCreated());
        return dto;
    }

    public ItemRequestDto toDtoWithItems(ItemRequest request, List<Item> items) {
        ItemRequestDto dto = toDto(request);
        if (dto != null && items != null) {
            dto.setItems(items.stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public ItemRequest toModel(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }
        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setRequesterId(dto.getRequesterId());
        request.setCreated(dto.getCreated());
        return request;
    }
}