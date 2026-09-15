package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Test
    void createShouldForwardValidUser() throws Exception {
        when(userClient.create(any(UserDto.class)))
                .thenReturn(ok(Map.of("id", 1L)));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "Vadim",
                        "email": "vadim@example.ru"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userClient).create(argThat(user -> "Vadim".equals(user.getName())
        && "vadim@example.ru".equals(user.getEmail())
        ));
    }

    @Test
    void createShouldRejectMissingEmail() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
        .content("""
                        {
                        "name": "Vadim"
                        }
        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userClient, never()).create(any(UserDto.class));
    }

    @Test
    void updateShouldForwardPartialUser() throws Exception {
        when(userClient.update(any(Long.class), any(UserDto.class)))
                .thenReturn(ok(Map.of(
                        "id", 1L,
                        "name", "New name"
                )));

        mockMvc.perform(patch("/users/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                        {
                        "name": "New name"
                        }
        """))
                .andExpect(status().isOk());

        verify(userClient).update(argThat(id -> id == 1L),
                argThat(user -> "New name".equals(user.getName())));
    }

    @Test
    void findByIdShouldForwardRequest() throws Exception {
        when(userClient.findById(1L))
                .thenReturn(ok(Map.of("id", 1L)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userClient).findById(1L);

    }
    @Test
    void findAllShouldForwardRequest() throws Exception {
        when(userClient.findAll())
                .thenReturn(ok(Map.of("id", 1L)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).findAll();
    }

    @Test
    void deleteShouldForwardRequest() throws Exception {
        when(userClient.delete(1L))
                .thenReturn(ok(Map.of("result", "deleted")));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
        verify(userClient).delete(1L);
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok().body(body);
    }


}
