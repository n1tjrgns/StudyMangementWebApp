package com.jpa.studywebapp.infra.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile({"local","test"})
@Component
public class ConsoleEmailService implements  EmailService{ //콘솔에 메일을 보내기 위한 서비스

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("sent email: {}", emailMessage.getMessage());
    }
}
