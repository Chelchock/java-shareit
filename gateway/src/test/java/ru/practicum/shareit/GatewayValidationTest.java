package ru.practicum.shareit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class GatewayValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createUserRequiresEmailButPatchMayContainOnlyName() {
        UserDto userDto = new UserDto();
        userDto.setName("Новое имя");

        assertThat(validator.validate(userDto, OnCreate.class)).isNotEmpty();
    }

    @Test
    void createItemRejectsBlankDescriptionAndMissingAvailability() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Дрель");
        itemDto.setDescription("   ");
        assertThat(validator.validate(itemDto, OnCreate.class)).extracting(violation -> violation
                .getPropertyPath()
                .toString())
                .contains("description", "available");
    }

    @Test
    void bookingRejectsReversedPeriod() {
        BookItemRequestDto bookingDto = new BookItemRequestDto();

        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));
        assertThat(validator.validate(bookingDto, OnCreate.class)).extracting(violation -> violation
                .getPropertyPath().toString()).contains("periodValid");
    }
}
