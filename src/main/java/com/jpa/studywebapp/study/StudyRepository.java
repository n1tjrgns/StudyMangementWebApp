package com.jpa.studywebapp.study;

import com.jpa.studywebapp.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);

    @EntityGraph(value="Study.withAllRelations", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
}
