package com.jpa.studywebapp.settings;

import com.jpa.studywebapp.domain.Account;
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

    public NotificationForm(Account account) {
        this.studyCreatedByEmail = account.isStudyCreatedByEmail();
        this.studyCreatedByWeb = account.isStudyCreatedByWeb();
        this.studyEnrollmentResultByEmail = account.isStudyEnrollmentResultByEmail();
        this.studyEnrollmentResultByWeb = account.isStudyEnrollmentResultByWeb();
        this.studyUpdatedByEmail = account.isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = account.isStudyUpdatedByWeb();
    }
}
