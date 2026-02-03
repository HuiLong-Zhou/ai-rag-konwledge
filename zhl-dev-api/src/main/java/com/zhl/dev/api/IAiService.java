package com.zhl.dev.api;

import org.springframework.ai.chat.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * <p>
 *
 * @author zhl
 * @since 2026-02-03 9:52
 */
public interface IAiService {
    ChatResponse generate(String model, String message);

    Flux<ChatResponse> generateStream(String model, String message);
}
