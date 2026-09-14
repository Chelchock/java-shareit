package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {
    public ItemRequestClient(
            @Value("${shareit-server.url}") String serverUrl,
            RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/requests"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());

    }

    public ResponseEntity<Object> create(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> findOwn(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> findAll(long userid, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("from", from, "size", size);
        return get("/all?from={from}&size={size}", userid, parameters);
    }

    public ResponseEntity<Object> findById(long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}
