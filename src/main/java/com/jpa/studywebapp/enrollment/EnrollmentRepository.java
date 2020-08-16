package com.jpa.studywebapp.enrollment;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Enrollment;
import com.jpa.studywebapp.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Enrollment findByEventAndAccount(Event event, Account account);

    boolean existsByEventAndAccount(Event event, Account account);
}
