package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.account.QAccount;
import com.jpa.studywebapp.modules.tag.QTag;
import com.jpa.studywebapp.modules.zone.QZone;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension{

    //부모의 기본 생성자
    public StudyRepositoryExtensionImpl() {
        //super(domainClass);
        super(Study.class); //어떤 클래스를 넘겨줘야할지 알고 있으므로 수정
    }

    //querydls을 사용해서 검색 구현, 제목, 태그, 지역에 해당하는 내용 검색
    @Override
    public Page<Study> findByKeyword(String keyword, Pageable pageable) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct();
        //querydslRepositorySupport가 제공해주는 getQuerydsl 사용
        JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Study> fetchResults = pageableQuery.fetchResults();
        //pageImpl 구현체 생성
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }
}
