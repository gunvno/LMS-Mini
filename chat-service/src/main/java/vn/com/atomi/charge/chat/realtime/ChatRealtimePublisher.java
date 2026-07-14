package vn.com.atomi.charge.chat.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.com.atomi.charge.chat.model.response.ChatMessageResponse;
import vn.com.atomi.charge.chat.model.response.ChatRealtimeEventResponse;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String eventType, String conversationId, ChatMessageResponse message) {
        ChatRealtimeEventResponse event = new ChatRealtimeEventResponse(
                eventType, conversationId, message, LocalDateTime.now());
        Runnable publish = () -> messagingTemplate.convertAndSend(
                "/topic/chat/conversations/" + conversationId,
                event);

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publish.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish.run();
            }
        });
    }
}
