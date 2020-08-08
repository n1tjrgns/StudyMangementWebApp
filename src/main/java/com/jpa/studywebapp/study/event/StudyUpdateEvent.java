package com.jpa.studywebapp.study.event;

import com.jpa.studywebapp.domain.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyUpdateEvent {

    private final Study study;
    private final String message;
}
