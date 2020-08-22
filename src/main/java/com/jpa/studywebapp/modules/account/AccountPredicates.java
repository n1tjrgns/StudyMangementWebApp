package com.jpa.studywebapp.modules.account;

import com.jpa.studywebapp.modules.tag.Tag;
import com.jpa.studywebapp.modules.zone.Zone;
import com.querydsl.core.types.Predicate;

import java.util.Set;

public class AccountPredicates {

    //얘는 왜 static이지?? -> 단순히 static method로 사용하기 위함
    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones){
        //tags와 zones을 포함하고있는 아무 account를 달라
        QAccount qAccount = QAccount.account;
        return qAccount.zone.any().in(zones).and(qAccount.tags.any().in(tags));
    }
}
