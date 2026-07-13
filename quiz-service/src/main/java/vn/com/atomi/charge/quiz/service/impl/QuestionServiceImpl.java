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
import java.util.Map;

@Service
public class QuestionServiceImpl extends BaseService<QuestionRepository, QuestionDto, QuestionEntity, QuestionMapper>
implements QuestionService {
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizOwnershipService ownershipService;

    @Override
    public BaseResponse<Page<QuestionDto>> getAll(Map<String, String> params, Pageable pageable) {
        String quizId = params == null ? null : params.get("quizId");
        QuizEntity quiz = quizId == null ? null : quizRepository.findEntityById(quizId).orElse(null);
        if (quiz == null) {
            throw new org.springframework.security.access.AccessDeniedException("common.access_denied");
        }
        ownershipService.assertCanViewQuiz(quiz);
        return super.getAll(params, pageable);
    }

    @Override
    public BaseResponse<QuestionDto> getDetails(String id) {
        QuestionEntity question = repository.findEntityById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanViewQuestion(question);
        return BaseResponse.success(HttpStatus.OK, mapper.toDto(question));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuestionDto> createQuestion(BaseRequest<QuestionDto> dto, String QuizId) {
        QuizEntity quiz = quizRepository.findEntityById(QuizId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanManageCourse(quiz.getCourseId());
        response = new BaseResponse<>();
        try {
            getRequest();

            dto.getData().setQuizId(QuizId);
            dto.getData().setOrderIndex(nextOrderIndex(QuizId));

            if (isDuplicate(dto)) {
                String localizedMsg = i18n.getMessage("common.already_exists");
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, localizedMsg);
            }

            Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(QuizId);
            if(optionalQuiz.isEmpty()){
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("question.not_found"));
            }
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
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuestionDto> create(BaseRequest<QuestionDto> request) {
        String quizId = request.getData().getQuizId();
        QuizEntity quiz = quizRepository.findEntityById(quizId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanManageCourse(quiz.getCourseId());
        request.getData().setOrderIndex(nextOrderIndex(quizId));
        return super.create(request);
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
        request.getData().setQuizId(question.getQuizId());
        request.getData().setOrderIndex(question.getOrderIndex());
        return super.update(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuestionDto> delete(String id) {
        QuestionEntity question = repository.findEntityById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        assertCanManageQuestion(question);
        BaseResponse<QuestionDto> result = super.delete(id);
        normalizeQuestionOrder(question.getQuizId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuestionDto> delete(List<String> ids) {
        List<QuestionEntity> questions = ids.stream()
                .map(id -> repository.findEntityById(id)
                        .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied")))
                .toList();
        questions.forEach(this::assertCanManageQuestion);
        BaseResponse<QuestionDto> result = super.delete(ids);
        questions.stream()
                .map(QuestionEntity::getQuizId)
                .distinct()
                .forEach(this::normalizeQuestionOrder);
        return result;
    }

    private void assertCanManageQuestion(QuestionEntity question) {
        QuizEntity quiz = quizRepository.findEntityById(question.getQuizId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanManageCourse(quiz.getCourseId());
    }

    private int nextOrderIndex(String quizId) {
        normalizeQuestionOrder(quizId);
        return Math.toIntExact(repository.countByQuizIdAndDeletedAtIsNull(quizId)) + 1;
    }

    private int normalizeQuestionOrder(String quizId) {
        List<QuestionEntity> questions = repository
                .findByQuizIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(quizId);
        boolean changed = false;
        for (int index = 0; index < questions.size(); index++) {
            int expectedOrder = index + 1;
            QuestionEntity question = questions.get(index);
            if (question.getOrderIndex() == null || question.getOrderIndex() != expectedOrder) {
                question.setOrderIndex(expectedOrder);
                changed = true;
            }
        }
        if (changed) {
            repository.saveAll(questions);
        }
        return questions.size();
    }

    private void assertCanViewQuestion(QuestionEntity question) {
        QuizEntity quiz = quizRepository.findEntityById(question.getQuizId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("common.access_denied"));
        ownershipService.assertCanViewQuiz(quiz);
    }
}
