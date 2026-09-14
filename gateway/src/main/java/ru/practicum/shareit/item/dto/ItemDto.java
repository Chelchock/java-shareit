package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
public class ItemDto {
    private Long id;

    @NotNull(groups = OnCreate.class, message = "Название не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Название не может быть пустым")
    private String name;

    @NotNull(groups = OnCreate.class, message = "Описание не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Описание не может быть пустым")
    private String description;

    @NotNull(groups = OnCreate.class, message = "Статус доступности должен быть указан")
    private Boolean available;

    private Long requestId;
}
