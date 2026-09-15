package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void requestIsPersistedAndEnrichedWithResponseItem() {
        User requester = saveUser("Requester", "requester@example.ru");

        User owner = saveUser("Owner", "Owner@example.ru");

        ItemRequestDto requestInput = new ItemRequestDto();

        requestInput.setDescription("Новая аккумуляторная дрель");

        ItemRequestDto createdRequest = requestService.create(requester.getId(), requestInput);

        ItemDto itemInput = new ItemDto();
        itemInput.setName("Дрель");
        itemInput.setDescription("18V");
        itemInput.setAvailable(true);
        itemInput.setRequestId(createdRequest.getId());

        itemService.create(owner.getId(), itemInput);

        ItemRequestDto result = requestService.findById(owner.getId(), createdRequest.getId());

        assertThat(result.getDescription()).isEqualTo(requestInput.getDescription());

        assertThat(result.getCreated()).isNotNull();

        assertThat(result.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getName()).isEqualTo("Дрель");
            assertThat(item.getOwnerId()).isEqualTo(owner.getId());
            assertThat(item.getRequestId()).isEqualTo(createdRequest.getId());
        });

        assertThat(requestService.findByRequesterId(requester.getId()))
                .extracting(ItemRequestDto::getId)
                .containsExactly(createdRequest.getId());

        assertThat(requestService.findAll(owner.getId(), 0, 10))
                .extracting(ItemRequestDto::getId)
                .containsExactly(createdRequest.getId());
    }

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);

        return userRepository.save(user);
    }
}
