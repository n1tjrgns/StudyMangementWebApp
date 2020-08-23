package com.jpa.studywebapp.modules.main;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.CurrentUser;
import com.jpa.studywebapp.modules.study.Study;
import com.jpa.studywebapp.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if(account != null){
            model.addAttribute(account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    //스터디 검색 기능
    @GetMapping("/search/study")
    public String searchStudy(String keyword, Model model){
        List<Study> studyList = studyRepository.findByKeyword(keyword);
        model.addAttribute(studyList);
        model.addAttribute("keyword",keyword);

        return "search";
    }
}
