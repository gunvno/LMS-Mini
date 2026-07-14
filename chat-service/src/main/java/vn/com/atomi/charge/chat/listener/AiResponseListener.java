package vn.com.atomi.charge.chat.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.com.atomi.charge.chat.model.event.UserMessageCreatedEvent;
import vn.com.atomi.charge.chat.service.internal.AssistantMessageWriter;
import vn.com.atomi.charge.chat.service.interfaces.AiAssistantService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResponseListener {

    private final AiAssistantService aiAssistantService;
    private final AssistantMessageWriter messageWriter;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserMessage(UserMessageCreatedEvent event) {
        try {
            messageWriter.writeAnswer(
                    event.conversationId(),
                    aiAssistantService.answer(event.conversationId()));
        } catch (Exception exception) {
            log.error("AI response failed for conversation {}: {}",
                    event.conversationId(), exception.getMessage());
            messageWriter.writeError(event.conversationId());
        }
    }
}
