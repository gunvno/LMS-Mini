USE lms_quiz_service;

-- Chuẩn hóa các lượt làm cũ: score là phần trăm thay vì tổng điểm thô.
UPDATE tbl_quiz_attempts attempt
JOIN tbl_quizzes quiz ON quiz.id = attempt.quiz_id
JOIN (
    SELECT attempt_answer.attempt_id,
           SUM(CASE WHEN attempt_answer.is_correct = TRUE THEN question.score ELSE 0 END) AS earned_score,
           SUM(question.score) AS maximum_score
    FROM tbl_quiz_attempt_answers attempt_answer
    JOIN tbl_questions question ON question.id = attempt_answer.question_id
    WHERE attempt_answer.deleted_at IS NULL
      AND question.deleted_at IS NULL
    GROUP BY attempt_answer.attempt_id
) score_summary ON score_summary.attempt_id = attempt.id
SET attempt.score = ROUND(score_summary.earned_score * 100 / NULLIF(score_summary.maximum_score, 0), 2),
    attempt.passed = CASE
        WHEN score_summary.maximum_score > 0
            THEN ROUND(score_summary.earned_score * 100 / score_summary.maximum_score, 2) >= quiz.pass_score
        ELSE FALSE
    END,
    attempt.last_modified_date = CURRENT_TIMESTAMP
WHERE attempt.status = 'SUBMITTED'
  AND attempt.deleted_at IS NULL;

SELECT attempt.id, attempt.score, attempt.passed
FROM tbl_quiz_attempts attempt
WHERE attempt.status = 'SUBMITTED'
  AND attempt.deleted_at IS NULL
ORDER BY attempt.submitted_at DESC;
