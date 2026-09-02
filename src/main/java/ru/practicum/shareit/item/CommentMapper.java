package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setItemId(comment.getItemId());
        dto.setAuthorName(comment.getAuthorName());
        dto.setCreated(comment.getCreated());
        return dto;
    }

    public Comment toModel(CommentDto dto) {
        if (dto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(dto.getId());
        comment.setText(dto.getText());
        comment.setItemId(dto.getItemId());
        comment.setAuthorName(dto.getAuthorName());
        comment.setCreated(dto.getCreated());
        return comment;
    }
}
