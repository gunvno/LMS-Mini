package vn.com.atomi.charge.course.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.course.config.minio.MinioProperties;
import vn.com.atomi.charge.course.model.storage.StorageFile;
import vn.com.atomi.charge.course.model.storage.StorageUploadResult;
import vn.com.atomi.charge.course.service.interfaces.StorageService;

import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioStorageServiceImpl(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String folder) {
        try {
            ensureBucketExists();

            String originalName = file.getOriginalFilename();
            if (!StringUtils.hasText(originalName)) {
                originalName = "image";
            }
            originalName = StringUtils.cleanPath(originalName);
            String fileName = UUID.randomUUID() + "-" + originalName;
            String objectName = folder + "/" + fileName;

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            return new StorageUploadResult(
                fileName,
                objectName,
                buildFileUrl(objectName),
                file.getContentType(),
                file.getSize()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Upload file to MinIO failed", ex);
        }
    }

    @Override
    public StorageFile download(String filePath) {
        try {
            byte[] content = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(filePath)
                    .build()
            ).readAllBytes();

            return new StorageFile(content, null);
        } catch (Exception ex) {
            throw new RuntimeException("Download file from MinIO failed", ex);
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build()
        );
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build()
            );
        }
    }

    private String buildFileUrl(String objectName) {
        String baseUrl = properties.getUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + properties.getBucket() + "/" + objectName;
    }
}
