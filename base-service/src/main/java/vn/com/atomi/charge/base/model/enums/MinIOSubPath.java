package vn.com.atomi.charge.base.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum MinIOSubPath {
    BILL("bill"),
    USER("user"),
    NEWS("news"),
    DEVICE("device")
    ;

    private final String subPath;
}
