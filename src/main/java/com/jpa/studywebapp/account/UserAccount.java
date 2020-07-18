package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

//현재 인증된 사용자 정보를 참조하기 위한 클래스
@Getter
public class UserAccount extends User { //시큐리티에 있는 user 클래스 상

    private final Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
