package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.tag.Tag;
import com.jpa.studywebapp.modules.zone.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface StudyRepositoryExtension {

    Page<Study> findByKeyword(String keyword, Pageable pageable);

    List<Study> findByAccount(Set<Tag> tags, Set<Zone> zone);
}
