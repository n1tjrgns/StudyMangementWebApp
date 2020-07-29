package com.jpa.studywebapp.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa.studywebapp.account.AccountService;
import com.jpa.studywebapp.account.CurrentUser;
import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.settings.form.*;
import com.jpa.studywebapp.settings.validator.NicknameValidator;
import com.jpa.studywebapp.settings.validator.PasswordFormValidator;
import com.jpa.studywebapp.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//프로필 수정 컨트롤러
@Controller
@RequiredArgsConstructor
public class SettingController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper; //fasterxml을 기본적으로 의존성이 들어와있

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinder2(WebDataBinder webDataBinder){
        webDataBinder.addValidators(nicknameValidator);
    }

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

    @GetMapping("/settings/password")
    public String passwordUpdateForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return "settings/password";
    }

    @PostMapping("/settings/password")
    public String passwordUpdate(@CurrentUser Account account, Model model, RedirectAttributes attributes
                                        , @Valid PasswordForm passwordForm, Errors errors){

        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");

        return "redirect:/settings/password";
    }

    @GetMapping("/settings/notifications")
    public String notificationForm(@CurrentUser Account account, Model model){

        model.addAttribute(account);
        //ModelMapper 사용으로 인해 쓸모없어짐
        //model.addAttribute(new NotificationForm(account));
        //두 코드는 같은 코드. account를 Notification을 사용해서 값을 채우는데 사용하라
        model.addAttribute(modelMapper.map(account, NotificationForm.class));

        return "settings/notifications";
    }

    @PostMapping("/settings/notifications")
    public String notificationUpdate(@CurrentUser Account account, Model model
                                        , @Valid NotificationForm notificationForm, Errors errors, RedirectAttributes attributes){

        if(errors.hasErrors()){
            model.addAttribute(account);
            return "/settings/notifications";
        }

        accountService.updateNotification(account, notificationForm);
        attributes.addFlashAttribute("message","알림설정이 수정되었습니다.");

        return "redirect:/settings/notifications";
    }

    @GetMapping("/settings/account")
    public String nicknameForm(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new NicknameForm(account));

        return "settings/account";
    }

    @PostMapping("/settings/account")
    public String nicknameUpdate(@CurrentUser Account account, Model model
                                , @Valid NicknameForm nicknameForm, Errors errors, RedirectAttributes attributes){

        if(errors.hasErrors()){
            model.addAttribute(account);
            return "settings/account";
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임이 변경되었습니다.");

        return "redirect:/settings/account";
    }

    //태그 컨트롤러
    @GetMapping("/settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {

        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        //model에 리스트로 데이터를 넘기는 방법
        //실제 데이터는 List<String> tags = List.of("spring","jpa")가 된다.
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        //전체 태그 가져오기
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "/settings/tags";
    }

    //태그 ajax add
    @PostMapping("/settings/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm){

        String title = tagForm.getTagTitle();

        //Optional을 사용 할 경우
        /*Tag tag = tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
                .title(tagForm.getTagTitle())
                .build()));*/

        Tag tag = tagRepository.findByTitle(title);

        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(title).build());
        }

        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    //태그 삭제
    @PostMapping("/settings/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm){

        String title = tagForm.getTagTitle();

        //Optional을 사용 할 경우
        /*Tag tag = tagRepository.findByTitle(title).orElseGet(() -> tagRepository.save(Tag.builder()
                .title(tagForm.getTagTitle())
                .build()));*/

        Tag tag = tagRepository.findByTitle(title);

        if(tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }


}
