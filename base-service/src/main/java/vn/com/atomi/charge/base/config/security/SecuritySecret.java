package vn.com.atomi.charge.base.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class SecuritySecret {

  @Value("${spring.security.pepper}")
  private String pepper;
}