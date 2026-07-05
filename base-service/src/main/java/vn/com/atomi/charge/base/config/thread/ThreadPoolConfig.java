package vn.com.atomi.charge.base.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20); // Số luồng xử lý song song
  }
}
