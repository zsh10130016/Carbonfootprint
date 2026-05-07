package com.zhangsihan.carbonfootprint.service;

import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.config.EmailVerificationProperties;
import com.zhangsihan.carbonfootprint.enums.EmailCodePurpose;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;
    private final EmailVerificationProperties properties;
    private final Map<String, VerificationCode> codes = new ConcurrentHashMap<>();

    public void sendRegisterCode(String email) {
        sendCode(email, EmailCodePurpose.REGISTER);
    }

    public void sendResetPasswordCode(String email) {
        sendCode(email, EmailCodePurpose.RESET_PASSWORD);
    }

    public void sendCode(String email, EmailCodePurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        String cacheKey = buildCacheKey(purpose, normalizedEmail);
        VerificationCode existing = codes.get(cacheKey);
        Instant now = Instant.now();
        if (existing != null && now.isBefore(existing.lastSentAt().plus(properties.getResendInterval()))) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码发送太频繁，请稍后再试");
        }

        String code = StringUtils.hasText(properties.getFixedCode())
                ? properties.getFixedCode().trim()
                : String.format("%06d", RANDOM.nextInt(1_000_000));
        SimpleMailMessage message = new SimpleMailMessage();
        if (StringUtils.hasText(properties.getFrom())) {
            message.setFrom(properties.getFrom().trim());
        }
        message.setTo(normalizedEmail);
        message.setSubject("个人碳足迹系统邮箱验证码");
        message.setText("你的" + purpose.getDisplayName() + "验证码是：" + code + "\n\n验证码 10 分钟内有效，请勿转发给他人。");

        try {
            mailSender.send(message);
            codes.put(cacheKey, new VerificationCode(code, now, now.plus(properties.getExpiresIn())));
        } catch (MailAuthenticationException exception) {
            log.warn("Email account authentication failed, email={}, purpose={}", normalizedEmail, purpose, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "邮箱账号认证失败，请检查 163 SMTP 授权码是否正确或已开启 SMTP 服务");
        } catch (MailException exception) {
            log.warn("Email verification code send failed, email={}, purpose={}", normalizedEmail, purpose, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "验证码发送失败，请检查邮箱配置");
        }
    }

    public void verifyRegisterCode(String email, String code) {
        verifyCode(email, code, EmailCodePurpose.REGISTER);
    }

    public void verifyResetPasswordCode(String email, String code) {
        verifyCode(email, code, EmailCodePurpose.RESET_PASSWORD);
    }

    public void verifyCode(String email, String code, EmailCodePurpose purpose) {
        String normalizedEmail = normalizeEmail(email);
        String cacheKey = buildCacheKey(purpose, normalizedEmail);
        VerificationCode verificationCode = codes.get(cacheKey);
        if (verificationCode == null || Instant.now().isAfter(verificationCode.expiresAt())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已过期，请重新获取");
        }
        if (!verificationCode.code().equals(code == null ? "" : code.trim())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "邮箱验证码不正确");
        }
        codes.remove(cacheKey);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildCacheKey(EmailCodePurpose purpose, String normalizedEmail) {
        return purpose.name() + ":" + normalizedEmail;
    }

    private record VerificationCode(String code, Instant lastSentAt, Instant expiresAt) {
    }
}
