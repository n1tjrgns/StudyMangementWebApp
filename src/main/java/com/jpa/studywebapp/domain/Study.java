package com.jpa.studywebapp.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
}
