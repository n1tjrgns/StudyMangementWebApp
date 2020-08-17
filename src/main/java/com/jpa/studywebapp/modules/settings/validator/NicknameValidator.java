package com.jpa.studywebapp.modules.settings.validator;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.AccountRepository;
import com.jpa.studywebapp.modules.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;

        Account nickname = accountRepository.findByNickname(nicknameForm.getNickname());

        if(nickname != null){
            errors.rejectValue("nickname", "wrong.nickname", "닉네임을 변경할 수 없습니다.");
        }
    }
}
