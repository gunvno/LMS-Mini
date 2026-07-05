package vn.com.atomi.charge.base.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class I18nService {

  private static final Locale DEFAULT_LOCALE = new Locale("vi");

  @Autowired
  private MessageSource messageSource;

  public String getMessage(String key, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();

    if (locale == null || locale.getLanguage() == null || locale.getLanguage().isBlank()) {
      locale = DEFAULT_LOCALE;
    }

    try {
      return messageSource.getMessage(key, args, locale);
    } catch (Exception ex) {
      return key;
    }
  }
}
