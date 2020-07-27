package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.settings.form.NotificationForm;
import com.jpa.studywebapp.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    //@Transactional //트랜잭션을 붙여줘야하는 이유 따로 정리함.
    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateEmailCheckToken();
        sendSignUpConfirmEmail(newAccount);

        return newAccount;
    }

    //리팩토링
    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword())) // 패스워드 암호화
                .studyCreatedByWeb(true)
                .studyEnrollmentResultByWeb(true)
                .studyUpdatedByWeb(true)
                .build();

        return accountRepository.save(account);
    }

    //메소드 안에 기능이 너무 많아서 리팩토링
    public void sendSignUpConfirmEmail(Account newAccount) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(newAccount.getEmail());
        simpleMailMessage.setSubject("회원가입 인증 메일입니다.");
        simpleMailMessage.setText("/check-email-token?token="+newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail() );

        javaMailSender.send(simpleMailMessage);
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
        emailAccount.generateEmailCheckToken();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(emailAccount.getEmail());
        simpleMailMessage.setSubject("로그인 링크.");
        simpleMailMessage.setText("/login-by-email?token="+emailAccount.getEmailCheckToken() +
                "&email=" + emailAccount.getEmail() );

        javaMailSender.send(simpleMailMessage);
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
        return byId.orElseThrow(NullPointerException::new).getTags();
    }
}
