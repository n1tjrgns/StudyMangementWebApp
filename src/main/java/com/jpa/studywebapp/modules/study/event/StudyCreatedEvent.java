package com.jpa.studywebapp.modules.study.event;

import com.jpa.studywebapp.modules.study.Study;
import lombok.Getter;

@Getter
public class StudyCreatedEvent {

    private Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
