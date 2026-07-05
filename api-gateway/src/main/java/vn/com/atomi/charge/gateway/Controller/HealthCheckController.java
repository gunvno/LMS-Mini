package vn.com.atomi.charge.gateway.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import vn.com.atomi.charge.gateway.service.HealthCheckService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthCheckController {

  @Autowired
  HealthCheckService healthCheckService;

  @GetMapping(value = "/health-check", produces = MediaType.APPLICATION_JSON_VALUE)
  public Health getHealthCheck() {
    return healthCheckService.getHealthCheck();
  }
}
