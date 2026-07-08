package vn.com.atomi.charge.course.model.storage;

public record StorageFile(
    byte[] content,
    String contentType
) {
}
