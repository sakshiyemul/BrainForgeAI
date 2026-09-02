package com.sakshi.brainforgeai.ai;

import com.sakshi.brainforgeai.entity.ChatMessage;
import java.util.List;
import java.util.function.Consumer;

public interface AiService {

    String generateAnswer(String question);

    String generateChatAnswer(List<ChatMessage> messages);

    void streamChatAnswer(List<ChatMessage> messages, Consumer<String> chunkConsumer);
}
