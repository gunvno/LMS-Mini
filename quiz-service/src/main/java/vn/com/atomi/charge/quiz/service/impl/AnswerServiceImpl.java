package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.base.util.StringUtil;
import vn.com.atomi.charge.quiz.mapper.AnswerMapper;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.service.interfaces.AnswerService;

import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class AnswerServiceImpl extends BaseService<AnswerRepository, AnswerDto, AnswerEntity, AnswerMapper>
implements AnswerService {
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizOwnershipService ownershipService;

    @Override
    public BaseResponse<Page<AnswerDto>> getAll(Map<String, String> params, Pageable pageable) {
        String questionId = params == null ? null : params.get("questionId");
        QuestionEntity question = questionId == null ? null
                : questionRepository.findEntityById(questionId).orElse(null);
        if (question == null) {
            throw new org.springframework.security.access.AccessDeniedException("common.access_denied");
        }
        assertCanViewQuestion(question);
        BaseResponse<Page<AnswerDto>> result = super.getAll(params, pageable);
        if (ownershipService.isStudentRequest() && result.getData() != null) {
            result.getData().forEach(answer -> answer.setCorrect(null));
        }
        return result;
    }

    @Override
    public BaseResponse<AnswerDto> getDetails(String id) {
        AnswerEntity answer = repository.findEntityById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanViewAnswer(answer);
        AnswerDto dto = mapper.toDto(answer);
        if (ownershipService.isStudentRequest()) {
            dto.setCorrect(null);
        }
        return BaseResponse.success(HttpStatus.OK, dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AnswerDto> createAnswer(BaseRequest<AnswerDto> dto, String questionId){
        QuestionEntity question = questionRepository.findEntityById(questionId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
        BaseResponse<AnswerDto> response = new BaseResponse<>();
        try {

            dto.getData().setQuestionId(questionId);
            dto.getData().setOrderIndex(nextOrderIndex(questionId));

            if (isDuplicate(dto)) {
                String localizedMsg = i18n.getMessage("common.already_exists");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            Optional<QuestionEntity> optionalQuestion = questionRepository.findEntityById(questionId);
            if(optionalQuestion.isEmpty()){
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("question.not_found"));
            }
            AnswerEntity saved = (AnswerEntity) repository.save(mapper.toEntity(dto.getData()));
            response.setStatus(HttpStatus.OK);
            response.setData((AnswerDto) mapper.toDto(saved));
        } catch (Exception ex) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setErrorCode(BaseErrorCode.FAILURE.getErrorCode());
            response.setMessage(StringUtil.beautyError(ex));
        }
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<AnswerDto> create(BaseRequest<AnswerDto> request) {
        String questionId = request.getData().getQuestionId();
        QuestionEntity question = questionRepository.findEntityById(questionId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
        request.getData().setOrderIndex(nextOrderIndex(questionId));
        return super.create(request);
    }
    @Override
    protected boolean isDuplicate(BaseRequest<AnswerDto> request) {
        AnswerDto dto = request.getData();

        if (dto.getQuestionId() == null || dto.getQuestionId().isBlank()
            || dto.getContent() == null || dto.getContent().isBlank()) {
            return false;
        }

        String questionId = dto.getQuestionId().trim();
        String content = dto.getContent().trim();

        if (dto.getId() == null) {
            return repository.existsByQuestionIdAndContentIgnoreCaseAndDeletedAtIsNull(questionId, content);
        }

        return repository.existsByQuestionIdAndContentIgnoreCaseAndIdNotAndDeletedAtIsNull(
            questionId,
            content,
            dto.getId()
        );
    }

    @Override
    public BaseResponse<AnswerDto> update(BaseRequest<AnswerDto> request) {
        AnswerEntity answer = repository.findEntityById(request.getData().getId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageAnswer(answer);
        request.getData().setQuestionId(answer.getQuestionId());
        request.getData().setOrderIndex(answer.getOrderIndex());
        return super.update(request);
    }

    @Override
    public BaseResponse<AnswerDto> delete(String id) {
        AnswerEntity answer = repository.findEntityById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageAnswer(answer);
        BaseResponse<AnswerDto> result = super.delete(id);
        normalizeAnswerOrder(answer.getQuestionId());
        return result;
    }

    @Override
    public BaseResponse<AnswerDto> delete(List<String> ids) {
        List<AnswerEntity> answers = ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied")))
                .toList();
        answers.forEach(this::assertCanManageAnswer);
        BaseResponse<AnswerDto> result = super.delete(ids);
        answers.stream().map(AnswerEntity::getQuestionId).distinct().forEach(this::normalizeAnswerOrder);
        return result;
    }

    private int nextOrderIndex(String questionId) {
        return normalizeAnswerOrder(questionId) + 1;
    }

    private int normalizeAnswerOrder(String questionId) {
        List<AnswerEntity> answers = repository
                .findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(questionId);
        boolean changed = false;
        for (int index = 0; index < answers.size(); index++) {
            int expectedOrder = index + 1;
            AnswerEntity answer = answers.get(index);
            if (answer.getOrderIndex() == null || answer.getOrderIndex() != expectedOrder) {
                answer.setOrderIndex(expectedOrder);
                changed = true;
            }
        }
        if (changed) {
            repository.saveAll(answers);
        }
        return answers.size();
    }

    private void assertCanManageAnswer(AnswerEntity answer) {
        QuestionEntity question = questionRepository.findEntityById(answer.getQuestionId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
    }

    private void assertCanManageQuestion(QuestionEntity question) {
        vn.com.atomi.charge.quiz.model.entity.QuizEntity quiz = quizRepository.findEntityById(question.getQuizId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanManageCourse(quiz.getCourseId());
    }

    private void assertCanViewAnswer(AnswerEntity answer) {
        QuestionEntity question = questionRepository.findEntityById(answer.getQuestionId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanViewQuestion(question);
    }

    private void assertCanViewQuestion(QuestionEntity question) {
        vn.com.atomi.charge.quiz.model.entity.QuizEntity quiz = quizRepository.findEntityById(question.getQuizId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanViewQuiz(quiz);
    }
}
