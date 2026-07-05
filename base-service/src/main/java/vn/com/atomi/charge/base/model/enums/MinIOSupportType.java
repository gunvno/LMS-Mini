package vn.com.atomi.charge.base.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum MinIOSupportType {
    IMAGE(Arrays.asList(".jpg", ".jpeg", ".png")),
    FILE(Arrays.asList(".pdf", ".docx"))
    ;

    private final List<String> listExtension;
}
