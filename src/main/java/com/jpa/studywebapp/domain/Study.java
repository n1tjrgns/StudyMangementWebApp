package com.jpa.studywebapp.domain;

import com.jpa.studywebapp.account.UserAccount;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

//엔티티 그래프 정
@NamedEntityGraph(name = "Study.withAllRelations", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members"),
})
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")
})
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>(); //관리자

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path; //url 정보

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting; // 인원 모지 여부

    private boolean published; //모임 공개 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; //배너 사용 여부

    public void addManager(Account account) {
        this.managers.add(account);
    }

    //뷰에서 직접 조회 할 메소드 정의
    public boolean isJoinable(UserAccount userAccount){ //참석 가능 여부
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount){ // 기존 멤버 여부
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount){ //기존 매니저 여부
        return this.managers.contains(userAccount.getAccount());
    }

    public boolean isManagedBy(Account account) {
        if(!managers.contains(account)) return false;
        else return true;
    }
}
