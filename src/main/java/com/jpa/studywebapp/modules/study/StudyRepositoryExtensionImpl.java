package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.account.QAccount;
import com.jpa.studywebapp.modules.tag.QTag;
import com.jpa.studywebapp.modules.zone.QZone;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements StudyRepositoryExtension{

    //부모의 기본 생성자
    public StudyRepositoryExtensionImpl() {
        //super(domainClass);
        super(Study.class); //어떤 클래스를 넘겨줘야할지 알고 있으므로 수정
    }

    //querydls을 사용해서 검색 구현, 제목, 태그, 지역에 해당하는 내용 검색
    @Override
    public List<Study> findByKeyword(String keyword) {
        QStudy study = QStudy.study;
        JPQLQuery<Study> query = from(study).where(study.published.isTrue()
                .and(study.title.containsIgnoreCase(keyword))
                .or(study.tags.any().title.containsIgnoreCase(keyword))
                .or(study.zones.any().localNameCity.containsIgnoreCase(keyword)))
                .leftJoin(study.tags, QTag.tag).fetchJoin()
                .leftJoin(study.zones, QZone.zone).fetchJoin()
                .leftJoin(study.members, QAccount.account).fetchJoin()
                .distinct();
        return query.fetch();
    }
}
