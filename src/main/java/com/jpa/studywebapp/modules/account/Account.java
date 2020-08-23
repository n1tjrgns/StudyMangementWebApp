package com.jpa.studywebapp.modules.account;

import com.jpa.studywebapp.modules.tag.Tag;
import com.jpa.studywebapp.modules.zone.Zone;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor @NoArgsConstructor
@Builder
public class Account {

    @Id @GeneratedValue
    private Long id;

    //닉네임과 이메일은 유니크 해야함
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    //이메일 인증 여부 플래그
    private boolean emailVerified;

    //이메일 체크 토큰값
    private String emailCheckToken;

    //이메일 토큰 생성시
    private LocalDateTime emailCheckTokenGeneratedAt;

    //가입 날짜
    private LocalDateTime joinedAt;

    //자기소개
    private String bio;

    private String url;

    private String occupation;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER) //로그인 시점에 같이 쓰일 수 있도록 EAGER로 가져옴
    @Type(type = "org.hibernate.type.TextType")
    private String profileImage;

    //알림설정 항목지
    //스터디 생성 여부 알림을 받을 수단
    private boolean studyCreatedByEmail;

    @Builder.Default
    private boolean studyCreatedByWeb = true;

    //스터디 가입 신청 결과를 받을 수단
    private boolean studyEnrollmentResultByEmail;

    @Builder.Default
    private boolean studyEnrollmentResultByWeb = true;

    //스터디 업데이트 결과를 받을 수단
    private boolean studyUpdatedByEmail;

    @Builder.Default
    private boolean studyUpdatedByWeb = true;

    @Builder.Default
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @ManyToMany
    private Set<Zone> zone = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now(); //토큰 생성시 생성시각 저장
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    //이메일 재전송 시각이 현재시각 기준 1시간이 흘렀는지 체크
    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }
}
