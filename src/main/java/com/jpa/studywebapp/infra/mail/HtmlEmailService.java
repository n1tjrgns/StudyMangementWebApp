package com.jpa.studywebapp.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Slf4j
@Profile({"dev","prod"})
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements  EmailService{

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        //인증 메일을 html로 보내기 위한 소스 수정
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8"); //메세지, 첨부파일 여부, 인코딩
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), true); //추후 html 사용시 true로
           javaMailSender.send(mimeMessage);
            log.info("sent email: {}", emailMessage.getMessage());
        } catch (MessagingException e) {
            log.error("failed to send email", e);
        }
    }
}
