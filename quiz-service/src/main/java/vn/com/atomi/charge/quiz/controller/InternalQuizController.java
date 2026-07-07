package vn.com.atomi.charge.quiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.internal.settingproviders.UseArnRegionProvider;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;

@RestController
@RequestMapping("/internal/v1/quizzes")
public class InternalQuizController {
    @Autowired
    private QuizService quizService;

    @GetMapping("course/{courseId}/required-result")
    public boolean completeQuizRequiredInCourse(@PathVariable String courseId){
        return quizService.completeQuizRequiredInCourse(courseId);
    }
}
