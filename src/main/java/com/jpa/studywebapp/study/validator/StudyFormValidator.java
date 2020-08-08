package com.jpa.studywebapp.study.validator;

import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.study.StudyRepository;
import com.jpa.studywebapp.study.form.StudyForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;


    @Override
    public boolean supports(Class<?> clazz) {
        return Study.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if(studyRepository.existsByPath(studyForm.getPath())){
            errors.rejectValue("path", "wrong.path", "해당 경로를 사용 할 수 없습니다.");
        }
    }
}
