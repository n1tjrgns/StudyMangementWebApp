package com.jpa.studywebapp.study;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;

    public Study createStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }
}
