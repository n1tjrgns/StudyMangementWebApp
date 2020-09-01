package com.jpa.studywebapp.modules.event;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.enrollment.EnrollmentRepository;
import com.jpa.studywebapp.modules.event.form.EventForm;
import com.jpa.studywebapp.modules.study.Study;
import com.jpa.studywebapp.modules.study.event.StudyUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Event updateEvent(Account account, Study study, Event event) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);

        //모임 알림 추가
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                event.getTitle() + "모임을 만들었습니다."));

        eventRepository.save(event);
        return event;
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList(); //모집 인원 변경시 변경된 인원이 자동 반영되도록
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                event.getTitle() + "모임 정보가 수정되었습니다."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(),
                event.getTitle() + "모임이 취소되었습니다."));
    }

    public void newEnrollment(Event event, Account account) {
        if(!enrollmentRepository.existsByEventAndAccount(event, account)){
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWatingEnrollment());
            enrollment.setAccount(account);
            event.addEnrollment(enrollment); //양방향 관계 설정을 위한 이벤트 객체에 enrollment 객체 추가
            enrollmentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if(!enrollment.isAttended()){ //출석체크를 했으면 참가 신청 취소를 하지 못하도록
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);

            //대기중인 인원중에 새로 참가 할 수 있는 인원이 있는 경우
            event.acceptTheFirstWaitingEnrollment();
        }
    }

    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accpet(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkinEnrollment(Event event, Enrollment enrollment) {
        enrollment.setAttended(true);
    }

    public void cancelCheckInEnrollment(Event event, Enrollment enrollment) {
        enrollment.setAttended(false);
    }
}
