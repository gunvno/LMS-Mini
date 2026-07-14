package vn.com.atomi.charge.chat.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.model.dto.CourseCatalogDto;
import vn.com.atomi.charge.chat.client.CourseClient;
import vn.com.atomi.charge.chat.model.dto.AiAnswer;
import vn.com.atomi.charge.chat.model.entity.ChatMessageEntity;
import vn.com.atomi.charge.chat.model.enums.ChatSenderType;
import vn.com.atomi.charge.chat.repository.ChatMessageRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class GeminiAiAssistantServiceTest {

    @Mock
    private CourseClient courseClient;
    @Mock
    private ChatMessageRepository messageRepository;

    private MockRestServiceServer server;
    private GeminiAiAssistantService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new GeminiAiAssistantService(
                courseClient,
                messageRepository,
                builder.baseUrl("https://generativelanguage.googleapis.com").build(),
                new ObjectMapper());
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "model", "gemini-test");
        ReflectionTestUtils.setField(service, "maxCatalogSize", 200);
        ReflectionTestUtils.setField(service, "maxOutputTokens", 800);
    }

    @Test
    void filtersRecommendationIdsAgainstCourseCatalog() {
        CourseCatalogDto course = new CourseCatalogDto(
                "course-java", "Java Backend", "Khóa thực hành", "BEGINNER",
                1200, new BigDecimal("1290000"));
        when(courseClient.getPublishedCatalog(200))
                .thenReturn(BaseResponse.success(org.springframework.http.HttpStatus.OK, List.of(course)));
        ChatMessageEntity userMessage = new ChatMessageEntity();
        userMessage.setSenderType(ChatSenderType.USER);
        userMessage.setContent("Có khóa Java dưới 1,5 triệu không?");
        when(messageRepository.findByConversationIdAndDeletedAtIsNullOrderByCreatedDateDesc(
                eq("conversation-a"), any(Pageable.class)))
                .thenReturn(List.of(userMessage));
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key"))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"answer\\":\\"Có khóa Java phù hợp.\\",\\"recommendedCourseIds\\":[\\"course-java\\",\\"fake-id\\"]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));

        AiAnswer answer = service.answer("conversation-a");

        assertThat(answer.content()).isEqualTo("Có khóa Java phù hợp.");
        assertThat(answer.recommendedCourses())
                .extracting(courseItem -> courseItem.id())
                .containsExactly("course-java");
        server.verify();
    }
}
