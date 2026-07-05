package vn.com.atomi.charge.gateway.scheduler;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsulReRegisterScheduler {

  @Value("${spring.cloud.consul.discovery.instance-id}")
  private String instanceId;

  @Value("${spring.cloud.consul.discovery.service-name}")
  private String serviceName;

  @Value("${spring.cloud.consul.host}")
  private String consulHost;

  @Value("${spring.cloud.consul.port}")
  private Integer consulPort;

  private ConsulClient consulClient;

  private final ConsulAutoServiceRegistration autoServiceRegistration;

  @Scheduled(fixedDelay = 10000) // kiểm tra mỗi 10s
  public void checkAndRegister() {
    try {
      if (this.consulClient == null) {
        consulClient = new ConsulClient(consulHost, consulPort);
      }

      // Lấy danh sách service đã đăng ký trên agent
      Response<List<CatalogService>> response =  consulClient.getCatalogService(serviceName, QueryParams.DEFAULT);
      List<CatalogService> instances = response.getValue();

      if(instances.isEmpty()) {
        autoServiceRegistration.stop();
        Thread.sleep(2000);
        autoServiceRegistration.start();
        return;
      }
      boolean instanceExists = false;
      for (CatalogService cs : instances) {
        String id = cs.getServiceId();
        if (instanceId.equalsIgnoreCase(id)) {
          instanceExists = true;
          break;
        }
      }

      if (!instanceExists) {
        log.warn("Instance [{}] missing in catalog. Re-registering...", instanceId );
        autoServiceRegistration.stop();
        Thread.sleep(2000);
        autoServiceRegistration.start();
        log.info("Instance [{}] re-registered.", instanceId);
      }
    } catch (Exception e) {
      log.error("Error in checkAndRegister: {}", ExceptionUtils.getStackTrace(e));
    }
  }
}
