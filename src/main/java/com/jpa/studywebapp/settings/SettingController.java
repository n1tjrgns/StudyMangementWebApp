package com.jpa.studywebapp.settings;

import com.jpa.studywebapp.account.AccountService;
import com.jpa.studywebapp.account.CurrentUser;
import com.jpa.studywebapp.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

//프로필 수정 컨트롤러
@Controller
@RequiredArgsConstructor
public class SettingController {

    private final AccountService accountService;

    @GetMapping("/settings/profile")
    public String profileUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new Profile(account));

        return "settings/profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors, Model model
                                    , RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);

            return "/settings/profile";
        }

        //데이터 변경 작업은 서비스 쪽에 위임
        accountService.updateProfile(account, profile);

        //스프링 mvc에서 제공해준다. 한번 사용하고 없어질 데이터
        attributes.addFlashAttribute("message", "수정되었습니다.");

        //사용자의 form submit이 다시 일어나지 않도록 redirect
        return "redirect:/" + "settings/profile";
    }
}