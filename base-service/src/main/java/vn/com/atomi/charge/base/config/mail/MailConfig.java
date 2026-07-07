package vn.com.atomi.charge.base.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

  @Value("${config.mail.host}")
  String host;

  @Value("${config.mail.port}")
  Integer port;

  @Value("${config.mail.username}")
  String username;

  @Value("${config.mail.password}")
  String password;

  @Value("${config.mail.smtp.auth:true}")
  Boolean smtpAuth;

  @Value("${config.mail.smtp.starttls-enable:true}")
  Boolean starttlsEnable;

  @Value("${config.mail.smtp.ssl-enable:false}")
  Boolean sslEnable;

  @Bean
  public JavaMailSender getJavaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(host);
    mailSender.setPort(port);

    mailSender.setUsername(username);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", smtpAuth.toString());
    props.put("mail.smtp.starttls.enable", starttlsEnable.toString());
    props.put("mail.smtp.ssl.enable", sslEnable.toString());
    props.put("mail.smtp.ssl.trust", host);

    return mailSender;
  }
}
