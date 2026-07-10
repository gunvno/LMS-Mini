package vn.com.atomi.charge.authn.service.interfaces;

public interface MailService {
    void sendOtp(String email, String subject, String otp, String purposeLabel);

    void send(String email, String subject, String content);
}
