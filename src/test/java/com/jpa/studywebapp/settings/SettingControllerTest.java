package com.jpa.studywebapp.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa.studywebapp.WithAccount;
import com.jpa.studywebapp.account.AccountRepository;
import com.jpa.studywebapp.account.AccountService;
import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.settings.form.TagForm;
import com.jpa.studywebapp.tag.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    //WithAccountSecurityContextFactory에서 매번 계정을 새로 만들기 때문에 테스트 후에는 데이터를 지워줘야한다.
    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    //@WithUserDetails(value = "n1tjrgns", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @WithAccount("n1tjrgns")
    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    void updateProfile() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("bio","자기소개 수정 테스트")
                .with(csrf())) //form 데이터를 테스트할 때는 csrf 토큰과 함께 해야한다.
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message")); //플래쉬 데이터 속성값이 존재하는지 확인

        Account n1tjrgns = accountRepository.findByNickname("n1tjrgns");
        assertEquals("자기소개 수정 테스트", n1tjrgns.getBio());
    }

    @WithAccount("n1tjrgns")
    @DisplayName("프로필 수정 - 입력값 에러")
    @Test
    void updateProfile_error() throws Exception {
        mockMvc.perform(post("/settings/profile")
                .param("bio","자기소개 수정 테스트자기소개 수정 테스트자기소개 수정 테스트자기소개 수정 테스트자기소개 수정 테스트자기소개 수정 테스트")
                .with(csrf())) //form 데이터를 테스트할 때는 csrf 토큰과 함께 해야한다.
                .andExpect(status().isOk())
                .andExpect(view().name("/settings/profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account n1tjrgns = accountRepository.findByNickname("n1tjrgns");
        assertNull(n1tjrgns.getBio());
    }

    @WithAccount("n1tjrgns")    //사용자가 없으면 시큐리티에 걸려서 테스트를 통과하지 못함. 단순 get 요청 테스트가 아님
    @DisplayName("인증된 사용자로 get 요청")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("n1tjrgns")
    @DisplayName("tagForm 테스트")
    @Test
    void tagForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    //ajax 데이터 테스트 하는 방법, 데이터 본문이 넘어옴
    @WithAccount("n1tjrgns")
    @DisplayName("계정에 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account newAccount = accountRepository.findByNickname("n1tjrgns");
        assertTrue(newAccount.getTags().contains(newTag));
    }

    @WithAccount("n1tjrgns")
    @DisplayName("계정에 태그 삭제")
    @Test
    void removeTag() throws Exception {

        Account n1tjrgns = accountRepository.findByNickname("n1tjrgns");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(n1tjrgns, newTag);

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/settings/tags/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(n1tjrgns.getTags().contains(newTag));
    }
}