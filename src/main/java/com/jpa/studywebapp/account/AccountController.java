package com.jpa.studywebapp.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String signUpForm(HttpServletRequest request, Model model){

        //속성명과 클래스 이름이 같다면 생략가능
       //model.addAttribute("signUpForm", new SignUpForm());
        model.addAttribute(new SignUpForm());

        return "account/sign-up";
    }
}
