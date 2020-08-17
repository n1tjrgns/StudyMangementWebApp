package com.jpa.studywebapp.modules.account;

import com.jpa.studywebapp.infra.mail.EmailMessage;
import com.jpa.studywebapp.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @DisplayName("회원 가입 화면 보이는성지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up")) //뷰가 존재하는지
                .andExpect(model().attributeExists("signUpForm")); //속성이 존재하는지
    }

    @DisplayName("회원 가입 폼 - 입력값 오류")
    @Test
    void signUpSubmit_with_wrong_input() throws Exception {
        mockMvc.perform(post("/sign-up")
            .param("nickname","n1tjrgns")
            .param("email","n1tjrgns@naver.com")
            .param("password","12345")
            .with(csrf()))  //타임리프 security를 사용할 때 폼 자체에서 csrf hidden value 를 같이 넘겨줘야함
            .andExpect(status().isOk())
            .andExpect(view().name("account/sign-up"));
    }

    @DisplayName("회원 가입 폼 - 입력값 정상")
    @Test
    void signUpSubmit_with_right_input() throws Exception {
        mockMvc.perform(post("/sign-up")
            .param("nickname","n1tjrgns")
            .param("email","n1tjrgns@naver.com")
            .param("password","12345678")
            .with(csrf()))  //타임리프 security를 사용할 때 폼 자체에서 csrf hidden value 를 같이 넘겨줘야함
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/"))
            .andExpect(authenticated().withUsername("n1tjrgns"));

        //assertTrue(accountRepository.existsByEmail("n1tjrgns@naver.com"));
        Account account = accountRepository.findByEmail("n1tjrgns@naver.com");
        assertNotNull(account);
        assertNotNull(account.getEmailCheckToken());
        assertNotEquals("123456",account.getPassword());

        //회원가입을 하고나면 mailsender의 send메소드가 호출이 됐는지 확인
        then(emailService).should().sendEmail((any(EmailMessage.class)));
    }

    //이메일 인증 테스트
    @DisplayName("이메일 인증 - 파라미터 잘못된 입력")
    @Test
    void checkedEmail_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
            .param("token","adsfasdfdasfasdfasdf")
            .param("email","email@email.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("account/checked-email"))
            .andExpect(model().attributeExists("error"))
            .andExpect(unauthenticated());
    }

    @DisplayName("이메일 인증 - 토큰 확인")
    @Test
    void checkedEmail_right_input() throws Exception {

        Account account = Account.builder()
                .nickname("n1tjrgns")
                .email("n1tjrgns@naver.com")
                .password("12345676")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token",newAccount.getEmailCheckToken())
                .param("email",newAccount.getEmail()))
                .andExpect(status().isOk())
                //TODO 왜 에러가 발생하면안되는데 발생해서 통과를 못하지? 이 부분 더 확인해봐야한다
                // 에러가 발생한 상황에서 view를 리턴해줬어야했는데 안해줌..
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("n1tjrgns"));
    }
}