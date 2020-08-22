package com.jpa.studywebapp.modules.notification;

import com.jpa.studywebapp.modules.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByAccountAndChecked(Account account, boolean b);
}
