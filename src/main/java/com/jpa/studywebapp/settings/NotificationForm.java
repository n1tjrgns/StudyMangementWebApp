package com.jpa.studywebapp.settings;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationForm {

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb = true;

    //스터디 가입 신청 결과를 받을 수단
    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb = true;

    //스터디 업데이트 결과an 을 수단
    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb = true;

    //ModelMapper 사용으로 인한 생성자 쓸모없어짐
    /*public NotificationForm(Account account) {
        this.studyCreatedByEmail = account.isStudyCreatedByEmail();
        this.studyCreatedByWeb = account.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = account.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = account.isStudyEnrollmentResultByWeb();
        this.studyUpdatedByEmail = account.isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = account.isStudyUpdatedByWeb();
    }*/
}
