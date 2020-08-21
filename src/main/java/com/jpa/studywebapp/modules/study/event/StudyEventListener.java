package com.jpa.studywebapp.modules.study.event;

import com.jpa.studywebapp.modules.account.AccountRepository;
import com.jpa.studywebapp.modules.study.Study;
import com.jpa.studywebapp.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Async
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){
        //현재 study에는 태그와 지역에 대한 정보가 빠져있어서 아래 객체로는 사용이 불가능하다.
        //Study study = studyCreatedEvent.getStudy();
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
    }
}
