package com.jpa.studywebapp.modules.study.event;

import com.jpa.studywebapp.infra.config.AppProperties;
import com.jpa.studywebapp.infra.mail.EmailMessage;
import com.jpa.studywebapp.infra.mail.EmailService;
import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.AccountPredicates;
import com.jpa.studywebapp.modules.account.AccountRepository;
import com.jpa.studywebapp.modules.notification.Notification;
import com.jpa.studywebapp.modules.notification.NotificationRepository;
import com.jpa.studywebapp.modules.notification.NotificationType;
import com.jpa.studywebapp.modules.study.Study;
import com.jpa.studywebapp.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){
        //현재 study에는 태그와 지역에 대한 정보가 빠져있어서 아래 객체로는 사용이 불가능하다.
        //Study study = studyCreatedEvent.getStudy();
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());

        //태그 정보와 지역정보를 가지고 있는 회원 조회
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if(account.isStudyCreatedByEmail()){
                //TODO 이메일 전송, AccountService에 만들어놨던 기능 재사용
                createEmail(study, account,"새로운 스터디가 생겼습니다.", study.getTitle()+"스터디가 생겼습니다.");
            }
            if(account.isStudyCreatedByWeb()){
                //TODO notification
                createNotification(study, account, study.getShortDescription(), NotificationType.STUDY_CREATED);
            }
        });
    }

    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent){
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());

        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());

        accounts.forEach(account -> {
            if(account.isStudyUpdatedByEmail()){
                // EMAIL
                createEmail(study, account,"새로운 스터디 소식이 있습니다 .", study.getTitle()+"스터디가 생겼습니다.");
            }

            if(account.isStudyUpdatedByWeb()){
                // ALim
                createNotification(study, account, studyUpdateEvent.getMessage(), NotificationType.STUDY_UPDATED);
            }
        });
    }

    private void createNotification(Study study, Account account, String message, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        try {
            notification.setLink("/study/" + study.getEncodedPath());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(message);
        notification.setAccount(account);
        notification.setNotificationType(notificationType);
        notificationRepository.save(notification);
    }

    private void createEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        try {
            context.setVariable("link","/study/"+study.getEncodedPath());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject(study.getTitle() + emailSubject)
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
