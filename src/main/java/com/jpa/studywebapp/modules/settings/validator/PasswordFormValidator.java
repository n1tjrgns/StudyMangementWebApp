package com.jpa.studywebapp.modules.settings.validator;

import com.jpa.studywebapp.modules.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator
{
    //검증할 form 클래스 지정
    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    //유효성 처리 할 부분
    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPassword", "wrong.value", "비밀번호가 일치하지 않습니다.");
        }
    }
}
