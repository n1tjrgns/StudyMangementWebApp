package com.jpa.studywebapp.domain;

import com.jpa.studywebapp.account.UserAccount;
import com.jpa.studywebapp.event.EventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NamedEntityGraph(name = "Event.withEnrollments", attributeNodes = {
        @NamedAttributeNode("enrollments")
})
@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Study study; //어느 스터디에 속한 이벤트인지

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime; //모임 생성 시간

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime; // 참여 마감 시간

    @Column(nullable = false)
    private LocalDateTime startDateTime; // 모임 시작 시간

    @Column(nullable = false)
    private LocalDateTime endDateTime; //모임 종료 시간

    @Column(nullable = true)
    private Integer limitOfEnrollments; //참가 신청 최대 인원 제한

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserAccount userAccount){
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount){
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }

    public boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for(Enrollment e : this.enrollments){
            if(e.getAccount().equals(account)){
                return true;
            }
        }
        return false;
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment e : this.enrollments) {
            if (e.getAccount().equals(account) && e.isAttended()) {
                return true;
            }
        }

        return false;
    }

    public boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    //잔여 참석자 수
    public int numberOfRemainSpots(){
        return (int) (this.limitOfEnrollments - this.enrollments.stream().filter(Enrollment::isAccepted).count());
    }

    //현재 참석자 수
    public long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    //참가 신청
    public boolean canAccept(Enrollment enrollment){
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    //참가 신청 취소
    public boolean canReject(Enrollment enrollment){
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }

    public boolean isAbleToAcceptWatingEnrollment() {
        //모집 방식이 선착순이고, 제한된 인원 만큼 사람이 차지 않으면 true
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    //양방향 관계 매핑
    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.setEvent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.setEvent(null);
    }

    public void acceptTheFirstWaitingEnrollment() {
        if(this.isAbleToAcceptWatingEnrollment()){
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if(enrollmentToAccept != null){
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    private Enrollment getTheFirstWaitingEnrollment() {
        for(Enrollment e : this.enrollments){
            if(!e.isAccepted()){
                return e;
            }
        }
        return null;
    }

    //대기중 인원이 존재 할 시 참석중으로 바꿔줌
    public void acceptWaitingList() {
        if(this.isAbleToAcceptWatingEnrollment()){
            List<Enrollment> waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }

    //대기중인 인원 구하기
    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
    }
}
