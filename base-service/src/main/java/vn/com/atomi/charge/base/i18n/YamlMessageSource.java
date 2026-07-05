package vn.com.atomi.charge.base.i18n;

import jakarta.annotation.Nonnull;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class YamlMessageSource extends AbstractMessageSource {
    private final String basename;
    private final Map<String, String> messages = new HashMap<>();

    public YamlMessageSource(String basename) {
        this.basename = basename;
        loadYamlFiles();
    }

    @Override
    protected MessageFormat resolveCode(@Nonnull String code, Locale locale) {
        String localeKey = code + "_" + locale.toString();
        String langKey = code + "_" + locale.getLanguage();

        String message = messages.get(localeKey);
        if (message == null) {
            message = messages.get(langKey);
        }
        if (message == null) {
            message = messages.get(code);
        }
        return (message != null) ? new MessageFormat(message, locale) : null;
    }

    private void loadYamlFiles() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources("classpath*:" + basename + "*.yml");
            Yaml yaml = new Yaml();

            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                try (InputStream is = resource.getInputStream()) {
                    Map<String, Object> data = yaml.load(is);
                    if (data != null) {
                        assert fileName != null;
                        flattenMap("", data, fileName.replace(".yml", ""));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML message files", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenMap(String prefix, Map<String, Object> map, String localeSuffix) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                flattenMap(key, (Map<String, Object>) entry.getValue(), localeSuffix);
            } else {
                String finalKey = key;
                if (!localeSuffix.equals("messages")) { // Ví dụ: messages_vi → vi
                    String[] parts = localeSuffix.split("_");
                    if (parts.length > 1) {
                        finalKey = key + "_" + parts[1]; // locale code
                    }
                }
                messages.put(finalKey, String.valueOf(entry.getValue()));
            }
        }
    }
}
