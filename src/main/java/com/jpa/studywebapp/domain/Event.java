package com.jpa.studywebapp.domain;

import com.jpa.studywebapp.account.UserAccount;
import com.jpa.studywebapp.event.EventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    public int numberOfRemainSpots(){
        return (int) (this.limitOfEnrollments - this.enrollments.stream().filter(Enrollment::isAccepted).count());
    }
}
