package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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
@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void createShouldForwardValidBooking() throws Exception {
        LocalDateTime start = LocalDateTime.now()
                .plusDays(1)
                .withNano(0);

        LocalDateTime end = start.plusHours(2);

        when(bookingClient.create(eq(2L), any(BookItemRequestDto.class)
        )).thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(post("/bookings")
                .header(USER_ID_HEADER, 2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "itemId": 5,
                        "start": "%s",
                        "end": "%s"
                        }
                        """.formatted(start,end)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingClient).create(eq(2L),
                org.mockito.ArgumentMatchers.argThat(
                        booking -> booking.getItemId() == 5L && start.equals(booking.getStart())
                                && end.equals(booking.getEnd()))
                );
    }

    @Test
    void createShouldRejectReversedPeriod() throws Exception {
        LocalDateTime start = LocalDateTime.now()
                .plusDays(2)
                .withNano(0);
        LocalDateTime end = start.minusDays(1);

        mockMvc.perform(post("/bookings")
        .header(USER_ID_HEADER, 2)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {
                    "itemId": 5,
                    "start": "%s",
                    "end": "%s"
                }
        """.formatted(start,end)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(bookingClient, never())
                .create(
                        eq(2L),
                        any(BookItemRequestDto.class)
                );
    }

    @Test
    void approveShouldForwardRequest() throws Exception {
        when(bookingClient.approve(1L, 10L, true)).thenReturn(ok(Map.of("id", 10L,
                "status", "APPROVED")));

        mockMvc.perform(patch("/bookings/10")
        .header(USER_ID_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("APPROVED"));

        verify(bookingClient).approve(1L, 10L, true);
    }

    @Test
    void findByIdShouldForwardRequest() throws Exception {
        when(bookingClient.findById(2L, 10L))
                .thenReturn(ok(Map.of("id", 10L)));

        mockMvc.perform(get("/bookings/10")
        .header(USER_ID_HEADER, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));

        verify(bookingClient).findById(2L, 10L);
    }

    @Test
    void findByBookerIdShouldUseDefaults() throws Exception {
        when(bookingClient.findByBooker(2L, BookingState.ALL, 0, 10)).thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/bookings")
                .header(USER_ID_HEADER, 2))
                .andExpect(status().isOk());

        verify(bookingClient).findByBooker(2L, BookingState.ALL, 0, 10);
    }

    @Test
    void findByOwnerShouldForwardStateAndPagination() throws Exception {
        when(bookingClient.findByOwner(1L, BookingState.FUTURE, 5, 2))
                .thenReturn(ok(Map.of("result", "ok")));

        mockMvc.perform(get("/bookings/owner")
                .header(USER_ID_HEADER, 1)
                .param("state", "future")
                .param("from", "5")
                .param("size", "2"))
                .andExpect(status().isOk());

        verify(bookingClient).findByOwner(1L, BookingState.FUTURE, 5, 2);
    }

    @Test
    void unknownStateShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                .header(USER_ID_HEADER, 2)
                .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unknown state: UNKNOWN"));

        verify(bookingClient, never()).findByBooker(eq(2L), any(BookingState.class), eq(0), eq(10));
    }

    private ResponseEntity<Object> ok(Object body) {
        return ResponseEntity.ok(body);
    }
}