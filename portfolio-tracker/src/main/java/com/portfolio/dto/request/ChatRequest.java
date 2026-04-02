package com.portfolio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Chat message request")
public class ChatRequest {

    @NotBlank
    @Schema(description = "User's message", example = "Should I sell my IT stocks given current market conditions?")
    private String message;

    @Schema(description = "Previous conversation messages for context (last 10 recommended)")
    private List<ChatMessage> history;

    @Data
    public static class ChatMessage {
        @Schema(description = "Role of the message sender", example = "user", allowableValues = {"user", "assistant"})
        private String role;

        @Schema(description = "Message content")
        private String content;
    }
}
