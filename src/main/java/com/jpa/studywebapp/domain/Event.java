package com.jpa.studywebapp.domain;

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

    @ManyToMany
    private Study study; //어느 스터디에 속한 이벤트인지

    @ManyToMany
    private Account account;

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
}
