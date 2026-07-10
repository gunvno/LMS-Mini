package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.quiz.mapper.QuestionMapper;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.service.interfaces.QuestionService;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionServiceImpl extends BaseService<QuestionRepository, QuestionDto, QuestionEntity, QuestionMapper>
implements QuestionService {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizOwnershipService ownershipService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuestionDto> createQuestion(BaseRequest<QuestionDto> dto, String QuizId) {
        response = new BaseResponse<>();
        try {
            getRequest();

            dto.getData().setQuizId(QuizId);

            if (isDuplicate(dto)) {
                String localizedMsg = i18n.getMessage("common.already_exists");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(QuizId);
            if(optionalQuiz.isEmpty()){
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("question.not_found"));
            }
            ownershipService.assertCanManageCourse(optionalQuiz.get().getCourseId());

            QuestionEntity saved = (QuestionEntity) repository.save(mapper.toEntity(dto.getData()));
            response.setStatus(HttpStatus.OK);
            response.setData((QuestionDto) mapper.toDto(saved));
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }
        return response;
    }
    @Override
    protected boolean isDuplicate(BaseRequest<QuestionDto> request) {
        QuestionDto dto = request.getData();

        if (dto.getQuizId() == null || dto.getQuizId().isBlank()
                || dto.getContent() == null || dto.getContent().isBlank()) {
            return false;
        }

        String quizId = dto.getQuizId().trim();
        String content = dto.getContent().trim();

        if (dto.getId() == null) {
            return repository.existsByQuizIdAndContentIgnoreCaseAndDeletedAtIsNull(quizId, content);
        }

        return repository.existsByQuizIdAndContentIgnoreCaseAndIdNotAndDeletedAtIsNull(
                quizId,
                content,
                dto.getId()
        );
    }

    @Override
    public BaseResponse<QuestionDto> update(BaseRequest<QuestionDto> request) {
        QuestionEntity question = repository.findEntityById(request.getData().getId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
        if (request.getData().getQuizId() != null) {
            QuizEntity targetQuiz = quizRepository.findEntityById(request.getData().getQuizId())
                    .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
            ownershipService.assertCanManageCourse(targetQuiz.getCourseId());
        }
        return super.update(request);
    }

    @Override
    public BaseResponse<QuestionDto> delete(String id) {
        QuestionEntity question = repository.findEntityById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
        return super.delete(id);
    }

    @Override
    public BaseResponse<QuestionDto> delete(List<String> ids) {
        ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied")))
                .forEach(this::assertCanManageQuestion);
        return super.delete(ids);
    }

    private void assertCanManageQuestion(QuestionEntity question) {
        QuizEntity quiz = quizRepository.findEntityById(question.getQuizId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanManageCourse(quiz.getCourseId());
    }
}
