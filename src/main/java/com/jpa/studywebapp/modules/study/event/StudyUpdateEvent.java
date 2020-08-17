package com.jpa.studywebapp.modules.study.event;

import com.jpa.studywebapp.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {

    private final Study study;
    private final String message;
}
