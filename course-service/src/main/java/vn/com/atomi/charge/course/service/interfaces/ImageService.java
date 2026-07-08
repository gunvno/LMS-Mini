package vn.com.atomi.charge.course.service.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.ImageMapper;
import vn.com.atomi.charge.course.model.dto.ImageDto;
import vn.com.atomi.charge.course.model.entity.ImageEntity;
import vn.com.atomi.charge.course.repository.ImageRepository;

public interface ImageService
    extends IBaseService<ImageRepository, ImageDto, ImageEntity, ImageMapper> {
    BaseResponse<ImageDto> uploadCourseImage(String courseId, MultipartFile file);

    BaseResponse<java.util.List<ImageDto>> getCourseImages(String courseId);

    ResponseEntity<byte[]> viewImage(String imageId, boolean attachment);

    ResponseEntity<byte[]> viewPrimaryCourseImage(String courseId);
}
