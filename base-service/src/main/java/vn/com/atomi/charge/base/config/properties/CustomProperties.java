package vn.com.atomi.charge.base.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "config")
public class CustomProperties {

    private WhiteListApi api;
}
