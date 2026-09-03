package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Component
public class ItemRequestMapper {

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

    public ItemRequestDto toDtoWithItems(ItemRequest request, List<ItemDto> items) {
        ItemRequestDto dto = toDto(request);
        if (dto != null && items != null) {
            dto.setItems(items);
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