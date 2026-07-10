package vn.com.atomi.charge.base.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

  @Value("${spring.mail.host:${config.mail.host}}")
  String host;

  @Value("${spring.mail.port:${config.mail.port}}")
  Integer port;

  @Value("${spring.mail.username:${config.mail.username}}")
  String username;

  @Value("${spring.mail.password:${config.mail.password}}")
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
    // Google displays App Passwords in groups separated by spaces; SMTP expects the compact value.
    mailSender.setPassword(password == null ? null : password
        .replaceAll("\\s+", "")
        .replace("\"", "")
        .replace("'", ""));

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.smtp.auth", smtpAuth.toString());
    props.put("mail.smtp.starttls.enable", starttlsEnable.toString());
    props.put("mail.smtp.ssl.enable", sslEnable.toString());
    props.put("mail.smtp.ssl.trust", host);
    props.put("mail.smtp.connectiontimeout", "5000");
    props.put("mail.smtp.timeout", "5000");
    props.put("mail.smtp.writetimeout", "5000");
    props.put("mail.smtp.starttls.required", "true");
    props.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

    return mailSender;
  }
}
