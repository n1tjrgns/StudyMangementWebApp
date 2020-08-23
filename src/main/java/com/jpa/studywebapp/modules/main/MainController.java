package com.jpa.studywebapp.modules.main;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.CurrentUser;
import com.jpa.studywebapp.modules.study.Study;
import com.jpa.studywebapp.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
    @GetMapping("/search/study") //size, page, sort
    public String searchStudy(@PageableDefault(size = 9, page = 0,sort = "publishedDateTime"
            , direction = Sort.Direction.ASC) Pageable pageable, String keyword, Model model){
        Page<Study> studyList = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage",studyList);
        model.addAttribute("keyword",keyword);
        model.addAttribute("sortProperty", pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");

        return "search";
    }
}
