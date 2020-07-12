package com.jpa.studywebapp.account;

import com.jpa.studywebapp.domain.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

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
            .andExpect(view().name("redirect:/"));

        //assertTrue(accountRepository.existsByEmail("n1tjrgns@naver.com"));
        Account account = accountRepository.findByEmail("n1tjrgns@naver.com");
        assertNotNull(account);
        assertNotEquals("123456",account.getPassword());

        //회원가입을 하고나면 mailsender의 send메소드가 호출이 됐는지 확인
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}