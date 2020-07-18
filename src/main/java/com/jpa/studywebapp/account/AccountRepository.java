package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

//select만 사용 할 곳에서는 readOnly를 줌으로써 조금이라도 성능 향상을 꾀할 수 있다.
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Account findByEmail(String email);

    Account findByNickname(String nickname);
}
