package com.jpa.studywebapp.modules.enrollment;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.event.Enrollment;
import com.jpa.studywebapp.modules.event.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Enrollment findByEventAndAccount(Event event, Account account);

    boolean existsByEventAndAccount(Event event, Account account);
}
