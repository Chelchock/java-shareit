package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

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
        .MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createShouldForwardValidItem() throws Exception {
        when(itemClient.create(
                eq(1L),
                any(ItemDto.class)
        )).thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(post("/items")
                .header(USER_ID_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "Дрель",
                        "description": "Аккумуляторная",
                        "available": true,
                        "requestId": 5
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(itemClient).create(eq(1L),
                argThat(item ->
                        "Дрель".equals(item.getName())
                && item.getAvailable()
                && item.getRequestId() == 5L
                )
        );
    }

    @Test
    void createShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/items")
                .header(USER_ID_HEADER, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name": "   ",
                        "description": "Описание",
                        "available": true
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(itemClient, never())
                .create(eq(1L), any(ItemDto.class));
    }

    @Test
    void updateShouldForwardPartialItem() throws Exception {
        when(itemClient.update(eq(1L), eq(10L), any(ItemDto.class)))
                .thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(patch("/items/10")
        .header(USER_ID_HEADER, 1)
        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "description": "Новое описание"
                        }
                        """))
                .andExpect(status().isOk());

        verify(itemClient).update(eq(1L), eq(10L), argThat(item -> "Новое описание"
                .equals(item.getDescription())));
    }

    @Test
    void findByIdShouldForwardRequest() throws Exception {
        when(itemClient.findById(1L, 10L)).thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(get("/items/10")
        .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
        verify(itemClient).findById(1L, 10L);
    }

    @Test
    void findByOwnerShouldUseDefaultPagination() throws Exception {
        when(itemClient.findByOwner(1L, 0, 10))
                .thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/items")
                .header(USER_ID_HEADER, 1))
                .andExpect(status().isOk());

        verify(itemClient).findByOwner(1L, 0, 10);
    }

    @Test
    void searchShouldForwardTextAndPagination() throws Exception {
        when(itemClient.search("дрель", 5, 2))
        .thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/items/search")
                .param("text","дрель")
                .param("from", "5")
                .param("size", "2"))
                .andExpect(status().isOk());

        verify(itemClient).search("дрель", 5, 2);
    }

    @Test
    void createCommentShouldForwardValidComment() throws Exception {
        when(itemClient.createComment(eq(2L),eq(10L),any(CommentDto.class)))
                .thenReturn(ok(Map.of("id", 7L)));

        mockMvc.perform(post("/items/10/comment")
        .header(USER_ID_HEADER, 2)
        .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "text": "Отличная вещь"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(itemClient).createComment(eq(2L), eq(10L),
                argThat(comment -> "Отличная вещь".equals(comment.getText())));
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }
}