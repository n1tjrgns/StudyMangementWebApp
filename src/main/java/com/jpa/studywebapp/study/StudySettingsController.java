package com.jpa.studywebapp.study;

import com.jpa.studywebapp.account.CurrentUser;
import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;

    //스터디 수정 페이지
    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, Model model, @PathVariable String path){
        //해당 스터디가 존재하고, 매니저 권한인지 확인.
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, Model model, @PathVariable String path, RedirectAttributes attributes,
                                        @Valid StudyDescriptionForm studyDescriptionForm, Errors errors) throws UnsupportedEncodingException {

        Study study = studyService.getStudyToUpdate(account, path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개가 수정되었습니다.");

        return "redirect:/study/" + URLEncoder.encode(study.getPath(), String.valueOf(StandardCharsets.UTF_8));
    }
}
