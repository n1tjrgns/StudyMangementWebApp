package com.jpa.studywebapp.modules.notification;

import com.jpa.studywebapp.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message; //짧은 메세지

    private boolean checked; //알림 확인 여부

    @ManyToOne
    private Account account; //누구에게

    private LocalDateTime createdLocalDateTime;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType; //새 스터디, 참여중인 스터디, 모임 참가 신청 결과
}
