package vn.com.atomi.charge.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.com.atomi.charge.gateway.config.ApiConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

  @Autowired
  RestTemplate restTemplate;

  @Autowired
  ApiConfig apiConfig;

  public Health getHealthCheck() {
    try {
      Map<String, Object> details = new HashMap<>();
      AtomicBoolean allUp = new AtomicBoolean(true);

      apiConfig.getApi().forEach((name, host) -> {
        try {
          String baseUrl = host.startsWith("http://") || host.startsWith("https://")
              ? host
              : "http://" + host;
          String healthUrl = baseUrl + "/actuator/health";
          Map healthResponse = restTemplate.getForObject(healthUrl, Map.class);

          String status = (String) healthResponse.get("status");
          details.put(name, status);

          if (!"UP".equals(status)) {
            allUp.set(false);
          }

        } catch (Exception ex) {
          details.put(name, "DOWN");
          allUp.set(false);
        }
      });

      return allUp.get()
        ? Health.up().withDetails(details).build()
        : Health.down().withDetails(details).build();

    } catch (Exception e) {
      return Health.down(e).build();
    }
  }
}
