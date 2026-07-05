package vn.com.atomi.charge.base.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class MessageConfig {

    @Bean
    public LocaleResolver localeResolver() {
      AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
      resolver.setDefaultLocale(Locale.forLanguageTag("vi"));
      return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName(HttpHeaders.ACCEPT_LANGUAGE);
        return lci;
    }

    @Bean
    public MessageSource messageSource() {
        return new YamlMessageSource("i18n/messages");
    }
}