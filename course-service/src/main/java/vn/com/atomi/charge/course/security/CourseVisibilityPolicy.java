package vn.com.atomi.charge.course.security;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.model.entity.LessonEntity;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;
import vn.com.atomi.charge.course.model.enums.ImageObjectType;

/**
 * Keeps instructor working copies private until they enter the review workflow.
 *
 * <p>{@link CourseStatus#DRAFT} remains the shared admin/reviewer draft state. Existing instructor
 * rows that still use DRAFT must be migrated explicitly after their role ownership is verified;
 * the course database cannot distinguish them safely from genuine admin drafts by itself.</p>
 */
public final class CourseVisibilityPolicy {

    private CourseVisibilityPolicy() {
    }

    public static boolean isVisibleToReviewer(CourseEntity course, String reviewerId) {
        return course != null && course.getStatus() != CourseStatus.INSTRUCTOR_DRAFT;
    }

    public static Specification<CourseEntity> reviewerVisibleCourses(String reviewerId) {
        return (root, query, cb) -> reviewerVisibleCoursePredicate(root, cb, reviewerId);
    }

    public static Specification<LessonEntity> reviewerVisibleLessons(String reviewerId) {
        return (root, query, cb) -> {
            Subquery<String> visibleCourseIds = query.subquery(String.class);
            Root<CourseEntity> course = visibleCourseIds.from(CourseEntity.class);
            visibleCourseIds.select(course.get("id"));
            visibleCourseIds.where(reviewerVisibleCoursePredicate(course, cb, reviewerId));
            return root.get("courseId").in(visibleCourseIds);
        };
    }

    public static Specification<LessonResourceEntity> reviewerVisibleResources(String reviewerId) {
        return (root, query, cb) -> {
            Subquery<String> visibleLessonIds = query.subquery(String.class);
            Root<LessonEntity> lesson = visibleLessonIds.from(LessonEntity.class);
            Root<CourseEntity> course = visibleLessonIds.from(CourseEntity.class);
            visibleLessonIds.select(lesson.get("id"));
            visibleLessonIds.where(
                    cb.equal(lesson.get("courseId"), course.get("id")),
                    cb.isNull(lesson.get("deletedAt")),
                    reviewerVisibleCoursePredicate(course, cb, reviewerId));
            return root.get("lessonId").in(visibleLessonIds);
        };
    }

    public static Specification<ImageEntity> reviewerVisibleImages(String reviewerId) {
        return (root, query, cb) -> {
            Subquery<String> visibleCourseIds = query.subquery(String.class);
            Root<CourseEntity> course = visibleCourseIds.from(CourseEntity.class);
            visibleCourseIds.select(course.get("id"));
            visibleCourseIds.where(reviewerVisibleCoursePredicate(course, cb, reviewerId));

            Subquery<String> visibleLessonIds = query.subquery(String.class);
            Root<LessonEntity> lesson = visibleLessonIds.from(LessonEntity.class);
            Root<CourseEntity> lessonCourse = visibleLessonIds.from(CourseEntity.class);
            visibleLessonIds.select(lesson.get("id"));
            visibleLessonIds.where(
                    cb.equal(lesson.get("courseId"), lessonCourse.get("id")),
                    cb.isNull(lesson.get("deletedAt")),
                    reviewerVisibleCoursePredicate(lessonCourse, cb, reviewerId));

            return cb.or(
                    cb.and(
                            cb.equal(root.get("objectType"), ImageObjectType.COURSE),
                            root.get("objectId").in(visibleCourseIds)),
                    cb.and(
                            cb.equal(root.get("objectType"), ImageObjectType.LESSON),
                            root.get("objectId").in(visibleLessonIds)),
                    cb.not(root.get("objectType").in(
                            ImageObjectType.COURSE,
                            ImageObjectType.LESSON)));
        };
    }

    private static Predicate reviewerVisibleCoursePredicate(
            From<?, CourseEntity> course,
            CriteriaBuilder cb,
            String reviewerId
    ) {
        return cb.and(
                cb.isNull(course.get("deletedAt")),
                cb.notEqual(course.get("status"), CourseStatus.INSTRUCTOR_DRAFT));
    }
}
