package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.practicum.shareit.validation.OnCreate;

@Data
public class UserDto {
    private Long id;

    @NotNull(groups = OnCreate.class, message = "Имя не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Имя не может быть пустым")
    private String name;

    @NotNull(groups = OnCreate.class, message = "Email не может быть пустым")
    @Pattern(regexp = ".*\\S.*", message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    private String email;

}
