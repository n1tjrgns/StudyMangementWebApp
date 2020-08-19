package com.jpa.studywebapp.infra.mail;

public interface EmailService {

    void sendEmail(EmailMessage emailMessage);
}
