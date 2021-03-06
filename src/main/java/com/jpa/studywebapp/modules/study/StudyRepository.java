package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

    boolean existsByPath(String path);

    @EntityGraph(value="Study.withAllRelations", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    //With는 Data Jpa 에서 지원하지 않는 키워드이기 때문에 무시된다. 명시적인 이름을 위해 이렇게 작성한다.
    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithMemebersByPath(String path);

    Study findStudyOnlyByPath(String path);

    @EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithTagsAndZonesById(Long id);

    //이렇게 사용해도 동일한 기능
    @EntityGraph(attributePaths = {"members","managers"})
    Study findStudyWithManagersAndMembersById(Long id);

    List<Study> findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findTop5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findTop5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}
