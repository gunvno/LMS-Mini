package vn.com.atomi.charge.course.model.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RejectCourseRequest {
    private String courseId;
    private String reasonReject;
}
