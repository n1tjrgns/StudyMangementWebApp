package com.jpa.studywebapp.modules.main;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.AccountRepository;
import com.jpa.studywebapp.modules.account.CurrentUser;
import com.jpa.studywebapp.modules.enrollment.EnrollmentRepository;
import com.jpa.studywebapp.modules.event.Enrollment;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model){
        if(account != null){
            //해당 계정이 가지고있는 태그와 지역정보 조회
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);

            //참석 확정된 계정들의 목록 조회
            List<Enrollment> enrollmentList = enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true);
            model.addAttribute(enrollmentList);

            //계정의 관심 태그, 지역정보 조회
            List<Study> studyList = studyRepository.findByAccount(accountLoaded.getTags(), accountLoaded.getZone());
            model.addAttribute("studyList", studyList);

            //관리중인 스터디 조회
            List<Study> studyManageOf = studyRepository.findTop5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
            model.addAttribute("studyManagerOf", studyManageOf);

            //참여중인 스터디 조
            List<Study> studyMemberOf = studyRepository.findTop5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false);
            model.addAttribute("studyMemberOf", studyMemberOf);

            return "index-after-login";
        }else{
            List<Study> studyList = studyRepository.findTop9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false);
            model.addAttribute("studyList", studyList);
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
