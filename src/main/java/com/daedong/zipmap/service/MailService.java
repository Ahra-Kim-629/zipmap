package com.daedong.zipmap.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public void sendPasswordResetMail(String email, String resetLink) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(email);
        helper.setSubject("비밀번호 재설정");
        helper.setText("<p>비밀번호를 재설정하기 위해 다음 링크를 클릭하세요.</p>"
                + "<a href=\"" + resetLink + "\">링크</a>", true);
        helper.setFrom(adminEmail);
        mailSender.send(message);
    }
}
