package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

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

        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword()) //TODO 암호화
                .studyCreatedByWeb(true)
                .stuydyEnrollmentByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        Account newAccount = accountRepository.save(account);

        newAccount.generateEmailCheckToken();

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(newAccount.getEmail());
        simpleMailMessage.setSubject("회원가입 인증 메일입니다.");
        simpleMailMessage.setText("/check-email-token?token="+newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail() );

        javaMailSender.send(simpleMailMessage);

        //정상통과시 메인 페이지로
       return "redirect:/";
    }
}
