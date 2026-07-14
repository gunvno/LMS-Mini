package vn.com.atomi.charge.chat.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import vn.com.atomi.charge.chat.model.response.SupportMessageResponse;
import vn.com.atomi.charge.chat.model.response.SupportRealtimeEventResponse;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SupportChatRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String eventType, String conversationId, SupportMessageResponse message) {
        SupportRealtimeEventResponse event = new SupportRealtimeEventResponse(
                eventType, conversationId, message, LocalDateTime.now());
        Runnable task = () -> messagingTemplate.convertAndSend(
                "/topic/support/conversations/" + conversationId,
                event);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
