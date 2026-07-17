package vn.com.atomi.charge.course.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MultipartConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withInitializer(new ConfigDataApplicationContextInitializer());

    @Test
    void courseUploadLimitOverridesBaseConfiguration() {
        contextRunner.run(context -> {
            assertThat(context.getEnvironment()
                .getProperty("spring.servlet.multipart.max-file-size"))
                .isEqualTo("200MB");
            assertThat(context.getEnvironment()
                .getProperty("spring.servlet.multipart.max-request-size"))
                .isEqualTo("210MB");
        });
    }
}
