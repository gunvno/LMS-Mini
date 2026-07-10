package vn.com.atomi.charge.payment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import vn.com.atomi.charge.base.model.exception.BusinessException;
import vn.com.atomi.charge.payment.config.PayosProperties;
import vn.com.atomi.charge.payment.model.dto.PayosApiResponse;
import vn.com.atomi.charge.payment.model.dto.PayosPaymentLinkResponse;
import vn.com.atomi.charge.payment.model.dto.PayosWebhookRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayosClient {
    private static final String SIGNATURE_ALGORITHM = "HmacSHA256";

    private final PayosProperties payosProperties;
    private final RestClient.Builder restClientBuilder;

    public boolean configured() {
        return payosProperties.configured();
    }

    public PayosPaymentLinkResponse createPaymentLink(Long orderCode, int amount, String description) {
        if (!configured()) {
            throw new BusinessException("PAYOS_NOT_CONFIGURED", "payos.not_configured");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("cancelUrl", payosProperties.cancelUrl());
        body.put("description", description);
        body.put("orderCode", orderCode);
        body.put("returnUrl", payosProperties.returnUrl());
        body.put("signature", sign(body));

        try {
            PayosApiResponse<PayosPaymentLinkResponse> response = restClientBuilder.build()
                    .post()
                    .uri(payosProperties.baseUrl() + "/v2/payment-requests")
                    .header("x-client-id", payosProperties.clientId())
                    .header("x-api-key", payosProperties.apiKey())
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null
                    || !"00".equals(response.getCode())
                    || response.getData() == null
                    || !StringUtils.hasText(response.getData().getCheckoutUrl())) {
                log.warn("PayOS create link failed. orderCode={}, code={}, desc={}",
                        orderCode,
                        response == null ? null : response.getCode(),
                        response == null ? null : response.getDesc());
                throw new BusinessException("PAYOS_CREATE_FAILED", "payos.create_failed");
            }
            return response.getData();
        } catch (RestClientResponseException exception) {
            log.warn("PayOS create link HTTP failed. orderCode={}, status={}, body={}",
                    orderCode, exception.getStatusCode(), exception.getResponseBodyAsString());
            throw new BusinessException("PAYOS_CREATE_FAILED", "payos.create_failed");
        } catch (RestClientException exception) {
            log.warn("PayOS create link request failed. orderCode={}, error={}", orderCode, exception.getMessage());
            throw new BusinessException("PAYOS_CREATE_FAILED", "payos.create_failed");
        }
    }

    public PayosPaymentLinkResponse getPaymentLink(Long orderCode) {
        if (!configured()) {
            throw new BusinessException("PAYOS_NOT_CONFIGURED", "payos.not_configured");
        }
        try {
            PayosApiResponse<PayosPaymentLinkResponse> response = restClientBuilder.build()
                    .get()
                    .uri(payosProperties.baseUrl() + "/v2/payment-requests/" + orderCode)
                    .header("x-client-id", payosProperties.clientId())
                    .header("x-api-key", payosProperties.apiKey())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (response == null || !"00".equals(response.getCode()) || response.getData() == null) {
                throw new BusinessException("PAYOS_PAYMENT_NOT_FOUND", "payos.payment_not_found");
            }
            return response.getData();
        } catch (RestClientResponseException exception) {
            log.warn("PayOS get payment HTTP failed. orderCode={}, status={}", orderCode, exception.getStatusCode());
            throw new BusinessException("PAYOS_PAYMENT_LOOKUP_FAILED", "payos.payment_lookup_failed");
        } catch (RestClientException exception) {
            log.warn("PayOS get payment request failed. orderCode={}, error={}", orderCode, exception.getMessage());
            throw new BusinessException("PAYOS_PAYMENT_LOOKUP_FAILED", "payos.payment_lookup_failed");
        }
    }

    public boolean verifyWebhook(PayosWebhookRequest request) {
        if (!configured()
                || request == null
                || request.getData() == null
                || !StringUtils.hasText(request.getSignature())) {
            return false;
        }
        return sign(request.getData()).equalsIgnoreCase(request.getSignature());
    }

    private String sign(Map<String, Object> data) {
        String rawData = new TreeMap<>(data).entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !"signature".equals(entry.getKey()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        try {
            Mac hmac = Mac.getInstance(SIGNATURE_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    payosProperties.checksumKey().getBytes(StandardCharsets.UTF_8),
                    SIGNATURE_ALGORITHM);
            hmac.init(keySpec);
            byte[] bytes = hmac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format(Locale.ROOT, "%02x", b));
            }
            return hex.toString();
        } catch (Exception exception) {
            throw new BusinessException("PAYOS_SIGN_FAILED", "payos.sign_failed");
        }
    }
}
