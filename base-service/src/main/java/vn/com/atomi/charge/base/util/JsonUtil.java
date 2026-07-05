package vn.com.atomi.charge.base.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper objectMapper = initObjectMapper();

    public static String convertObjectToJson(Object object) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("convertObjectToJson error: {}", Util.beautyError(e));
        }
        return null;
    }

    public static <T> T convertJsonToObject(String value, Class<T> typeParameterClass) {
        return parseJsonToObject(value, typeParameterClass);
    }

    public static <T> T parseJsonToObject(String value, Class<T> typeParameterClass) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.readValue(value, typeParameterClass);
        } catch (Exception e) {
            log.error("parseJsonToObject error: {}", Util.beautyError(e));
        }
        return null;
    }

    public static <T> T convertObjectToObject(Object value, Class<T> typeParameterClass) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.convertValue(value, typeParameterClass);
        } catch (Exception e) {
            log.error("convertObjectToObject error: {}", Util.beautyError(e));
        }
        return null;
    }

    public static <T> List<T> convertObjectToList(Object value, Class<T> typeParameterClass) {
        List<T> listResponse = null;
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, typeParameterClass);
            listResponse = objectMapper.convertValue(value, listType);
        } catch (Exception e) {
            log.error("convertObjectToList error: {}", Util.beautyError(e));
        }
        if (listResponse == null) {
            listResponse = new ArrayList<>();
        }
        return listResponse;
    }

    public static <T> List<T> convertJsonToList(String value, Class<T> typeParameterClass) {
        List<T> listResponse = null;
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, typeParameterClass);
            listResponse = objectMapper.readValue(value, listType);
        } catch (Exception e) {
            log.error("convertJsonToList error: {}", Util.beautyError(e));
        }
        if (listResponse == null) {
            listResponse = new ArrayList<>();
        }
        return listResponse;
    }

    public static MultiValueMap<String, Object> convertObjectToMultiValueMap(Object request) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
            Map<String, Object> maps = objectMapper.convertValue(request, new TypeReference<Map<String, Object>>() {
            });
            parameters.setAll(maps);
            return parameters;
        } catch (Exception e) {
            log.error("convertObjectToMultiValueMap error: {}", Util.beautyError(e));
        }
        return null;
    }

    public static Map<String, Object> convertObjectToMap(Object request) {
        try {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper.convertValue(request, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("convertObjectToMap error: {}", Util.beautyError(e));
        }
        return null;
    }

    public static ObjectMapper initObjectMapper() {
        return new ObjectMapper().findAndRegisterModules()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
    }
}
