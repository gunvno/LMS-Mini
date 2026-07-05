package vn.com.atomi.charge.course.model.storage;

public record StorageUploadResult(
    String fileName,
    String filePath,
    String fileUrl,
    String contentType,
    Long fileSize
) {
}
