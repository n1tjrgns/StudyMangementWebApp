package com.jpa.studywebapp.modules.account;

import com.jpa.studywebapp.infra.config.AppProperties;
import com.jpa.studywebapp.infra.mail.EmailMessage;
import com.jpa.studywebapp.infra.mail.EmailService;
import com.jpa.studywebapp.modules.settings.form.NotificationForm;
import com.jpa.studywebapp.modules.settings.form.Profile;
import com.jpa.studywebapp.modules.tag.Tag;
import com.jpa.studywebapp.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine; // simple-link 템플릿을 사용하기 위한 주입
    private final AppProperties appProperties;

    //@Transactional //트랜잭션을 붙여줘야하는 이유 따로 정리함.
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);

        //현재 이메일 서비스 로직에서 throw를 던지지 않아 상관이 없지만, 던진다면 transaction에 의해 회원가입까지 롤백 될 것이다.
        //이 점 기억해두고 따로 처리를 하던지 해야함.
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    //리팩토링
    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        /*Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // 패스워드 암호화
                .studyCreatedByWeb(true) //엔티티에서 초기값을 지정해줘도 사실상 적용되지 않아 이렇게 지정해줘야한다. 따라서 이 부분을 리팩토링 한다.
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();*/
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);

        account.generateEmailCheckToken(); //update 쿼리를 줄이기 위해서 이메일 토큰 생성시점을 회원가입 시점으로 변경

        return accountRepository.save(account);
    }

    //메소드 안에 기능이 너무 많아서 리팩토링
    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token="+newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("회원가입 인증 메일입니다.")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    //로그인 권한 부
    public void login(Account account) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                //account.getNickname(),
                new UserAccount(account),
                account.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);

    }

    //이메일과 닉네임으로 로그
    @Transactional(readOnly = true) //읽기 전용으로 성능 향상을 꾀할 수 있음.
    @Override
    public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {

        Account account = accountRepository.findByEmail(emailOrNickName);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickName);
        }

        if (account == null) throw new UsernameNotFoundException(emailOrNickName);

        //앞서 만들었던 principal 객체를 리턴해줘야한다.
        return new UserAccount(account);
    }

    //회원가입 완료시 자동 로그
    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account); //자로그인 기능
    }

    //프로필 수정
    public void updateProfile(Account account, Profile profile) {
        account.setBio(profile.getBio());
        account.setUrl(profile.getUrl());
        account.setOccupation(profile.getOccupation());
        account.setLocation(profile.getLocation());
        account.setProfileImage(profile.getProfileImage());
        accountRepository.save(account); //merge
    }

    // 비밀번호 업데이트
    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account); //merge
    }

    public void updateNotification(Account account, NotificationForm notificationForm) {

        //ModelMapper 사용으로 인해 쓸모없어짐
        /*account.setStudyCreatedByEmail(notificationForm.isStudyCreatedByEmail());
        account.setStudyCreatedByWeb(notificationForm.isStudyCreatedByWeb());
        account.setStudyUpdatedByEmail(notificationForm.isStudyUpdatedByEmail());
        account.setStudyUpdatedByWeb(notificationForm.isStudyUpdatedByWeb());
        account.setStudyEnrollmentResultByEmail(notificationForm.isStudyEnrollmentResultByEmail());
        account.setStudyEnrollmentResultByWeb(notificationForm.isStudyEnrollmentResultByWeb());*/

        //위 set을 아래 한줄로 대체 할 수 있음.
        modelMapper.map(notificationForm, account);
        accountRepository.save(account); //merge
    }

    //닉네임 업데이트
    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account); //닉네임 업데이트시 로그인을 시켜줘야 프로필의 닉네임이 새로 반영됨
    }

    //이메일 발송
    public void sendLoginLink(Account emailAccount) {

        Context context = new Context();
        context.setVariable("link", "/login-by-email?token="+emailAccount.getEmailCheckToken() +
                "&email=" + emailAccount.getEmail());
        context.setVariable("nickname", emailAccount.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(emailAccount.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        //여기의 account도 detached임을 주의, lazyloading도 불가능함
        //그래서 먼저 읽어와야한다. eager 방식
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));

        //lazyloading -> accountRepository.getOne() 무조건 lazyloading
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        //없으면 에러를 던지고 있으면 태그를 가져온다
        return byId.orElseThrow(NoSuchElementException::new).getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow(NullPointerException::new).getZone();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a ->a.getZone().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZone().remove(zone));
    }

    public Account getAccount(String nickname) {
        Account account = accountRepository.findByNickname(nickname);
        if(account == null){
            throw new IllegalArgumentException(account + "에 해당하는 사용자가 없습니다");
        }
        return account;
    }
}
