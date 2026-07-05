package vn.com.atomi.charge.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@ComponentScan(basePackages = {"vn.com.atomi.charge"})
@SpringBootApplication
@EnableFeignClients(basePackages = {"vn.com.atomi.charge"})
@EntityScan(basePackages = {"vn.com.atomi.charge"})
@EnableJpaRepositories(basePackages = {"vn.com.atomi.charge"})
@EnableDiscoveryClient
@EnableScheduling
public class AuthorizationServiceApplication {

    public static void main(String[] args) {
      TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
      SpringApplication.run(AuthorizationServiceApplication.class, args);
    }

}
