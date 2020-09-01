package com.jpa.studywebapp.modules.main;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler
    public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request, RuntimeException e){
        if(account != null){
            log.info("account : {} request : {}", account.getNickname(), request.getRequestURI());
        }else{
            log.info("request : {}", request.getRequestURI());
        }

        log.error("bad request", e);
        return "error";
    }
}
