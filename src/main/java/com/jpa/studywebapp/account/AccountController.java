package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){

        //속성명과 클래스 이름이 같다면 생략가능
       //model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm());

        return "account/sign-up";
    }

    //form에 대한 validation 검증
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if(errors.hasErrors()){
            return "account/sign-up";
        }

        //컨트롤러에서는 서비스에서 무슨 로직을 실행하는지 알 필요가없다.
        //서비스 뒤로 숨긴다.
        accountService.processNewAccount(signUpForm);

        //정상통과시 메인 페이지로
       return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        String view = "account/checked-Email";
        Account account = accountRepository.findByEmail(email);

        if(account == null){
            model.addAttribute("error", "wrong.email");
        }

        if(!account.getEmailCheckToken().equals(token)){
            model.addAttribute("error", "wrong.token");
        }

        //정상적인 이메일인 경우
        account.setEmailVerified(true);
        account.setJoinedAt(LocalDateTime.now());

        //뷰에 반환해줄 내용
        model.addAttribute("nickname", account.getNickname());
        model.addAttribute("numberOfUser", accountRepository.count());

        return view;
    }
}
