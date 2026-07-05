package vn.com.atomi.charge.gateway.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifySignRequestDto {

    private String deviceId;

    private String data;

    private String signature;
}
