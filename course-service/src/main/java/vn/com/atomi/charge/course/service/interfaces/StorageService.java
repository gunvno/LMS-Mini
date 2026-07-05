package vn.com.atomi.charge.course.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;

public interface StorageService {
    StorageUploadResult upload(MultipartFile file, String folder);
}
