package vn.com.atomi.charge.authn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authn.service.interfaces.MailService;
import vn.com.atomi.charge.base.model.exception.BusinessException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String subject, String otp, String purposeLabel) {
        send(email, subject, """
                Xin chào,

                Mã OTP của bạn là: %s

                Mục đích: %s
                Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với bất kỳ ai.

                Trân trọng,
                EduFlow
                """.formatted(otp, purposeLabel));
    }

    @Override
    public void send(String email, String subject, String content) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Could not send OTP email to {}: {}", email, ex.getMessage());
            throw new BusinessException("OTP_EMAIL_SEND_FAILED", "auth.otp_email_send_failed");
        }
    }
}
