package com.jpa.studywebapp.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
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
    private Set<Account> managers; //관리자

    @ManyToMany
    private Set<Account> members;

    @Column(unique = true)
    private String path; //url 정보

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER) // Lob타입은 기본값이 EAGER
    private String image;

    @ManyToMany
    private Set<Tag> tags;

    @ManyToMany
    private Set<Zone> zones;

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting; // 인원 모지 여부

    private boolean published; //모임 공개 여부

    private boolean closed; // 종료 여부

    private boolean useBanner; //배너 사용 여부
}
