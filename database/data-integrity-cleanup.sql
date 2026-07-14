-- Normalize lesson ordering per course.
UPDATE lms_course_service.tbl_lessons lesson
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY course_id
               ORDER BY COALESCE(order_index, 2147483647), created_date, id
           ) AS normalized_order
    FROM lms_course_service.tbl_lessons
    WHERE deleted_at IS NULL
) ordered ON ordered.id = lesson.id
SET lesson.order_index = ordered.normalized_order
WHERE lesson.deleted_at IS NULL
  AND lesson.order_index <> ordered.normalized_order;

-- Existing active quizzes that cannot be completed are returned to draft.
UPDATE lms_quiz_service.tbl_quizzes quiz
SET quiz.status = 'DRAFT'
WHERE quiz.deleted_at IS NULL
  AND quiz.status = 'ACTIVE'
  AND (
      NOT EXISTS (
          SELECT 1
          FROM lms_quiz_service.tbl_questions question
          WHERE question.quiz_id = quiz.id
            AND question.deleted_at IS NULL
      )
      OR EXISTS (
          SELECT 1
          FROM lms_quiz_service.tbl_questions question
          LEFT JOIN lms_quiz_service.tbl_answers answer
            ON answer.question_id = question.id
           AND answer.deleted_at IS NULL
          WHERE question.quiz_id = quiz.id
            AND question.deleted_at IS NULL
          GROUP BY question.id, question.score
          HAVING question.score IS NULL
              OR question.score <= 0
              OR COUNT(answer.id) < 2
              OR SUM(CASE WHEN answer.is_correct = 1 THEN 1 ELSE 0 END) <> 1
      )
  );
