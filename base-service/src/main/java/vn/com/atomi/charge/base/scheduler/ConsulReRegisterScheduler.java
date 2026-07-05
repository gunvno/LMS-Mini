package vn.com.atomi.charge.base.scheduler;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnBean(ConsulAutoServiceRegistration.class)
@RequiredArgsConstructor
public class ConsulReRegisterScheduler {

    @Value("${spring.cloud.consul.discovery.instance-id}")
    private String instanceId;

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
            Map<String, Service> services = consulClient.getAgentServices().getValue();
            if (!services.containsKey(instanceId)) {
                autoServiceRegistration.start();
            }
        } catch (Exception e) {
            log.error("Error in checkAndRegister: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
