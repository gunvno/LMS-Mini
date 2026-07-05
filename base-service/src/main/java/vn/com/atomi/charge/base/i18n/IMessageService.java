package vn.com.atomi.charge.base.i18n;

import java.util.List;

public interface IMessageService {

    String getMessage(String key);

    String getMessage(String key, String langCode);

    String getMessage(String key, Object[] args, String langCode);

    String getMessage(String key, Object[] args);
}