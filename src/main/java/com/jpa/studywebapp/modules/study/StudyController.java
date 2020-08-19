package com.jpa.studywebapp.modules.study;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.CurrentUser;
import com.jpa.studywebapp.modules.study.form.StudyForm;
import com.jpa.studywebapp.modules.study.validator.StudyFormValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController { //스터디 컨트롤러

    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final StudyFormValidator studyFormValidator;

    @InitBinder("studyForm")
    public void studyFormValidator(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }

    //스터디 개설 화면
    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model){

        model.addAttribute(account);
        model.addAttribute(new StudyForm());

        return "study/form";
    }

    //스터디 개설 submit
    @PostMapping("/new-study")
    public String createStudy(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors, Model model) throws UnsupportedEncodingException {
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createStudy(modelMapper.map(studyForm, Study.class), account);

        //url에 한글이 들어올 수 있기 때문에 인코딩을 해줘야한다.
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), String.valueOf(StandardCharsets.UTF_8));
    }

    //생성한 스터디 조회
    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model){
        //스터디를 가져왔는데 없는 경우에 대한 처리
        /*Study study = studyRepository.findByPath(path);
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }*/
        Study study = studyService.getStudy(path);

        model.addAttribute(account);
        model.addAttribute(study);

        return "study/view";
    }

    @GetMapping("/study/{path}/members")
    public String selectMembers(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/members";
    }

    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUser Account account, @PathVariable String path) throws UnsupportedEncodingException {
        Study study = studyRepository.findStudyWithMemebersByPath(path);
        studyService.addMember(account, study);
        return "redirect:/study/" + study.getURLEncoder(path) +"/members";
    }

    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path) throws UnsupportedEncodingException {
        Study study = studyRepository.findStudyWithMemebersByPath(path);
        studyService.removeMember(account, study);
        return "redirect:/study/" + study.getURLEncoder(path) +"/members";
    }
}
