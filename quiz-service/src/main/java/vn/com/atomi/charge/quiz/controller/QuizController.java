package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;

@RestController
@RequestMapping("/quiz")
@Tag(name = "Quiz", description = "CRUD APIs for quiz")
public class QuizController extends BaseController<QuizService, QuizDto> {
}
