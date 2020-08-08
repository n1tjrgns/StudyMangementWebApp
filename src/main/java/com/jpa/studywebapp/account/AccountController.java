package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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
        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account); //로그인 기능

        //정상통과시 메인 페이지로
       return "redirect:/";
    }

    @GetMapping("/check-email-token")
    @Transactional
    public String checkEmailToken(String token, String email, Model model){
        String view = "account/checked-email";
        Account account = accountRepository.findByEmail(email);

        if(account == null){
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if(!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return view;
        }

        //정상적인 이메일인 경우
        //리팩토링
        //서비스단으로 옮겨 트랜잭션안에서 관리
        accountService.completeSignUp(account);

        //뷰에 반환해줄 내용
        model.addAttribute("nickname", account.getNickname());
        model.addAttribute("numberOfUser", accountRepository.count());

        return view;
    }

    //가입 이메일 재전송 컨트롤러(회원가입은했지만, 이메일 인증을 안 한 회원)
    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model){

        //이메일만 있으면 되기 때문에
        model.addAttribute("email", account.getEmail());

        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendEmail(@CurrentUser Account account, Model model){
        if(!account.canSendConfirmEmail()){
            model.addAttribute("error", "이메일은 1시간에 한 번 만 보낼 수 있습니다.");
            model.addAttribute(account);
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }

    @GetMapping("/profile/{nickname}")
    public String profile(@PathVariable String nickname, Model model, @CurrentUser Account account){

        Account accountToView = accountService.getAccount(nickname);

        model.addAttribute(accountToView); //alias를 따로 지정해주지 않으면 해당 타입으로 저장됨, 여기서는 "account"
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        /*if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }*/

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/email-login";
    }

    @GetMapping("/login-by-email")
    @Transactional
    public String loginByEmail(String token, String email, Model model) {
        System.out.println("token : "+token);
        System.out.println("email : "+email);
        Account account = accountRepository.findByEmail(email);
        String view = "account/logged-in-by-email";
        if (account == null || !account.isValidToken(token)) {
            System.out.println("왜안돼");
            model.addAttribute("error", "로그인할 수 없습니다.");
            return view;
        }

        accountService.login(account);
        return view;
    }
}
