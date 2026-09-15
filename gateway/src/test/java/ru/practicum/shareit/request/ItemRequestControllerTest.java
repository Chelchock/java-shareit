package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;
@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void createShouldForwardValidRequest() throws Exception {
        when(requestClient.create(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(post("/requests")
        .header(USER_ID_HEADER, 1)
        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "description": "Нужна дрель"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(requestClient).create(eq(1L),
                argThat(request -> "Нужна дрель".equals(request.getDescription())));
    }

    @Test
    void createShouldRejectBlankDescription() throws Exception {
        mockMvc.perform(post("/requests")
                .header(USER_ID_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "description": "   "
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(requestClient, never())
        .create(eq(1L), any(ItemRequestDto.class));
    }

    @Test
    void findOwnShouldForwardRequest() throws Exception {
        when(requestClient.findOwn(1L))
                .thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/requests")
        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());

        verify(requestClient).findOwn(1L);
    }

    @Test
    void findAllShouldForwardPagination() throws Exception {
        when(requestClient.findAll(1L, 5, 2))
        .thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/requests/all")
                .header(USER_ID_HEADER, 1)
                .param("from", "5")
                .param("size", "2"))
                .andExpect(status().isOk());

        verify(requestClient).findAll(1L, 5, 2);
    }

    @Test
    void findByIdShouldForwardRequest() throws Exception {
        when(requestClient.findById(1L, 10L))
        .thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(get("/requests/10")
        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(requestClient).findById(1L, 10L);
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }
}
