package com.jpa.studywebapp.study;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.study.event.StudyUpdateEvent;
import com.jpa.studywebapp.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    public final StudyRepository studyRepository;
    public final ModelMapper modelMapper;
    public final ApplicationEventPublisher applicationEventPublisher;

    public Study createStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path); //스터디가 존재하는지 확인
        checkIfManager(account, study); // 매니저 권한인지 확인

        return study;
    }

    public void checkIfManager(Account account, Study study) {
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }

    public void checkIfExistingStudy(String path, Study study) {
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
        applicationEventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디 소개를 수정했습니다."));
    }
}
