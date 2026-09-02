package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;

    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;

    private Long itemId;
    private Long authorId;
    private String authorName;
    private LocalDateTime created;
}
