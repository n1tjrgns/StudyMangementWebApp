package com.jpa.studywebapp.modules.study;

import java.util.List;

public interface StudyRepositoryExtension {

    List<Study> findByKeyword(String keyword);
}
