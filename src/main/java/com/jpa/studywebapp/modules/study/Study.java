package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.UserAccount;
import com.jpa.studywebapp.modules.tag.Tag;
import com.jpa.studywebapp.modules.zone.Zone;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
@NamedEntityGraph(name = "Study.withManagers", attributeNodes = {
        @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withMembers", attributeNodes = {
        @NamedAttributeNode("members")
})
@NamedEntityGraph(name = "Study.withTagsAndZones", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones")
})

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Study {

    @Id @GeneratedValue
    private Long id;

    @Builder.Default
    @ManyToMany
    private Set<Account> managers = new HashSet<>(); //관리자

    @Builder.Default
    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path; //url 정보

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    @Type(type = "org.hibernate.type.TextType")
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    @Type(type = "org.hibernate.type.TextType")
    private String image;

    @Builder.Default
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting; // 인원 모지 여부

    private boolean published; //모임 공개 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; //배너 사용 여부

    @Builder.Default
    private int memberCount = 0;

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

    public void publish() {
        if(!this.closed && !this.published){ // 모임이 종료되지 않았고, 비공개중이면
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if(this.published && !this.closed){ // 모임이 공개되어있고, 종료되지 않았으면
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    //모집 변경 시각 1시간 경과 여부 체크
    public boolean canUpdateRecruit() {
        return this.published && this.recruitingUpdatedDateTime == null || //모집시간이 null이면 아직 최초 모집을 하지 않은 상태
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public void recruitStart() {
        if(canUpdateRecruit()){
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("1시간에 한 번만 스터디를 모집 할 수 있습니다.");
        }
    }

    public void recruitStop() {
        if(canUpdateRecruit()){
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        }else{
            throw new RuntimeException("1시간에 한 번만 스터디를 모집 할 수 있습니다.");
        }
    }

    public void updateNewPath(String newPath) {
        this.path = newPath;
    }

    public boolean isRemovable() {
        return !this.published; //공개되어있는 스터디는 삭제 할 수 없다.
    }

    public void addMember(Account account) {
        this.members.add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.members.remove(account);
        this.memberCount--;
    }

    public String getURLEncoder(String path) throws UnsupportedEncodingException {
        return URLEncoder.encode(path, String.valueOf(StandardCharsets.UTF_8));
    }

    public String getEncodedPath() throws UnsupportedEncodingException {
        return URLEncoder.encode(this.path, String.valueOf(StandardCharsets.UTF_8));
    }
}
