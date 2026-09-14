package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookItemRequestDto {

    @NotNull(message = "Id вещи должен быть указан")
    private Long itemId;

    @NotNull(message = "Дата начала должна быть указана")
    @Future(message = "Дата начала должна быть в будушем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания должна быть указана")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;

    @AssertTrue(message = "Дата начала должна быть раньше даты окончания")
    @JsonIgnore
    public boolean isPeriodValid() {
        return start == null || end == null || start.isBefore(end);
    }
}
