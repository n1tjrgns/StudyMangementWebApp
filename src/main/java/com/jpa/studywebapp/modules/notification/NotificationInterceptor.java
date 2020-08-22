package com.jpa.studywebapp.modules.notification;

import com.jpa.studywebapp.modules.account.Account;
import com.jpa.studywebapp.modules.account.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //인증정보가 있는 요청에만 응답을 해야한다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //모델 앤 뷰가 있고, 리다이렉트인 경우에는 리다이렉트 후 다시 핸들러 요청을 타기 때문에 리다이렉트 제외
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount){
            Account account = ((UserAccount) authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account, false);
            modelAndView.addObject("hasNotification", count > 0);
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:/") || modelAndView.getView() instanceof RedirectView;
    }
}
