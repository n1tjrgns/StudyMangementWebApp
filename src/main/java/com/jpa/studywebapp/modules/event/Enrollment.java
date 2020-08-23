package com.jpa.studywebapp.modules.event;

import com.jpa.studywebapp.modules.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@NamedEntityGraph(name = "Enrollment.withEventAndStudy", attributeNodes = {
        @NamedAttributeNode(value = "event", subgraph = "study")
},
        subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted; //참가 확정

    private boolean attended; //
}
