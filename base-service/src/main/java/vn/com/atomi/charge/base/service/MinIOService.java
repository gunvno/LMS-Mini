package vn.com.atomi.charge.base.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import vn.com.atomi.charge.base.model.enums.MinIOSubPath;
import vn.com.atomi.charge.base.model.enums.MinIOSupportType;
import vn.com.atomi.charge.base.model.exception.BusinessException;
import vn.com.atomi.charge.base.util.DateUtil;
import vn.com.atomi.charge.base.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOService {

    @Value("${config.minio.bucket}")
    private String bucket;

    private final S3Client s3Client;

    public String uploadFile(MinIOSubPath subPath, String fileName, MultipartFile fileContent, MinIOSupportType supportType) {
        log.info("uploadFile request: {}", fileName);
        String fileExtension = checkFileExtensionIsValid(fileContent);
        String filePath;
        try {
            switch (supportType) {
                case IMAGE:
                    if (!supportType.getListExtension().contains(fileExtension)) {
                        throw new BusinessException("common.invalid_file_extension");
                    }
                    break;
                default:
                    break;
            }
            fileName = String.join("_", fileName, DateUtil.genUnixTimeString());
            String fullFileName = String.join("", fileName, fileExtension);
            filePath = String.join("/", subPath.getSubPath(), fullFileName);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filePath)
                    .contentType(fileContent.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(fileContent.getBytes()));
        } catch (Exception e) {
            log.error("uploadFile error: {}", Util.beautyError(e));
            throw new BusinessException("common.upload_failed");
        }
        log.info("uploadFile completed");
        return filePath;
    }

    public byte[] downloadTemplate(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
              .bucket(bucket)
              .key(objectKey)
              .build();

            ResponseInputStream<GetObjectResponse> is = s3Client.getObject(request);

            try (InputStream in = is; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                in.transferTo(out);
                return out.toByteArray();
            }

        } catch (Exception e) {
            log.error("Download template from MinIO failed: {}", objectKey, e);
            throw new BusinessException("common.template_not_found");
        }
    }

    private String checkFileExtensionIsValid(MultipartFile fileContent) {
        Tika tika = new Tika();
        String actualExtension, suggestedExtension;
        try {
            String detectedMime = tika.detect(fileContent.getInputStream());
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType mimeType = allTypes.forName(detectedMime);
            suggestedExtension = mimeType.getExtension();
        } catch (Exception e) {
            throw new BusinessException("common.invalid_file_extension");
        }

        actualExtension = getExtension(fileContent.getOriginalFilename());
        if (suggestedExtension.equalsIgnoreCase(actualExtension)) {
            return actualExtension;
        } else {
            throw new BusinessException("common.invalid_file_extension");
        }
    }

    private static String getExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return null;
        }
        int i = fileName.lastIndexOf('.');
        return (i > 0) ? fileName.substring(i).toLowerCase() : "";
    }
}
