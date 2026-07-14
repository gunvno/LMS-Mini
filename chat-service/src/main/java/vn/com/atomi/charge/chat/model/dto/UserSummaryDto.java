package vn.com.atomi.charge.chat.model.dto;

public record UserSummaryDto(
        String id,
        String username,
        String email,
        String fullName,
        String status
) {
}
