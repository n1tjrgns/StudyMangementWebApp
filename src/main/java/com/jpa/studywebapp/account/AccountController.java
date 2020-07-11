package com.jpa.studywebapp.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AccountController {

    @GetMapping("/sign-up")
    public String signUp(HttpServletRequest request, Model model){

        /*String email = request.getParameter("email");
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");*/

        //model.addAttribute("email", email);

        return "account/sign-up";
    }
}
