package vn.com.atomi.charge.base.i18n;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageLocaleService implements IMessageService {

    private final MessageSource messageSource;

    private final HttpServletRequest httpServletRequest;

    @Override
    public String getMessage(String key) {
        try {
            return messageSource.getMessage(key, null, getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    public String getMessage(String key, String langCode) {
        try {
            Locale locale = Locale.forLanguageTag(langCode);
            return messageSource.getMessage(key, null, locale);
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    public String getMessage(String key, Object[] args, String langCode) {
        try {
            Locale locale = Locale.forLanguageTag(langCode);
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            return key;
        }
    }

    @Override
    public String getMessage(String key, Object[] args) {
        try {
            return messageSource.getMessage(key, args, getLocale());
        } catch (Exception e) {
            return key;
        }
    }

    private Locale getLocale() {
        try {
            String language = httpServletRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE);

            if (StringUtils.isEmpty(language)) {
                language = "vi";
            }

            return Locale.forLanguageTag(language);
        } catch (Exception e) {
            return Locale.forLanguageTag("vi");
        }
    }

}