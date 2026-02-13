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
        helper.setSubject("[SeoulRoom] 비밀번호 재설정을 위한 안내 메일입니다.");

        // HTML 이메일 본문 디자인
        String content = "<div style=\"margin: 0; padding: 0; background-color: #fdfcf9; font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif;\">"
                + "<table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border: 1px solid #eeeeee; border-top: 4px solid #e67e22;\">"
                + "  <tr>"
                + "    <td style=\"padding: 40px 30px; text-align: center;\">"
                + "      <h1 style=\"color: #e67e22; margin: 0; font-size: 28px; font-weight: bold;\">SeoulRoom</h1>"
                + "      <p style=\"color: #3e362e; font-size: 18px; margin-top: 10px; font-weight: bold;\">비밀번호 재설정 안내</p>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 0 30px 40px 30px; text-align: center; color: #666666; font-size: 15px; line-height: 1.6; Bryan\">"
                + "      안녕하세요, SeoulRoom입니다.<br>"
                + "      회원님의 계정 비밀번호 재설정 요청이 접수되었습니다.<br>"
                + "      아래의 버튼을 클릭하여 새로운 비밀번호를 설정해 주세요."
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 0 30px 40px 30px; text-align: center;\">"
                + "      <a href=\"" + resetLink + "\" style=\"display: inline-block; padding: 15px 35px; background-color: #e67e22; color: #ffffff; text-decoration: none; border-radius: 50px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 10px rgba(230, 126, 34, 0.3);\">비밀번호 재설정하기</a>"
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 0 30px 40px 30px; text-align: center; color: #999999; font-size: 13px; line-height: 1.5;\">"
                + "      본 메일은 발신 전용입니다. 만약 본인이 요청하지 않으셨다면<br>"
                + "      본 메일을 무시해 주세요. 비밀번호는 안전하게 유지됩니다."
                + "    </td>"
                + "  </tr>"
                + "  <tr>"
                + "    <td style=\"padding: 20px; text-align: center; background-color: #f8f9fa; color: #888888; font-size: 12px;\">"
                + "      © 2024 SeoulRoom Team. All Rights Reserved."
                + "    </td>"
                + "  </tr>"
                + "</table>"
                + "</div>";

        helper.setText(content, true); // true를 설정해야 HTML로 렌더링됨
        helper.setFrom(adminEmail);
        mailSender.send(message);
    }
}
