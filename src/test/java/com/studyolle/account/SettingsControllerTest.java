package com.studyolle.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.account.SignUpForm;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.settings.form.TagForm;
import com.studyolle.tag.TagRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;
    @Autowired AccountService accountService;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @WithAccount("yongbon")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get("/settings/tags"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whiteList"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(view().name("settings/tags"))
                .andExpect(authenticated());
    }

    @WithAccount("yongbon")
    @DisplayName("계정 태그 추가")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post("/settings/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());


        Tag newTag = tagRepository.findByTitle("newTag").get();
        assertNotNull(newTag);
        Account yongbon = accountRepository.findByNickname("yongbon");
        assertTrue(yongbon.getTags().contains(newTag));
    }

    @WithAccount("yongbon")
    @DisplayName("계정 태그 삭제")
    @Test
    void removeTag() throws Exception {
        Account yongbon = accountRepository.findByNickname("yongbon");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(yongbon, newTag);

        assertTrue(yongbon.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");
        mockMvc.perform(post("/settings/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(yongbon.getTags().contains(newTag));
    }

    @WithAccount("yongbon")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(view().name("settings/profile"))
                .andExpect(authenticated());
    }

    @WithAccount("yongbon")
    @DisplayName("프로필 수정 - 입력값 정상")
    @Test
    void updateProfile_with_correct_input() throws Exception {
        String bio = "짧은 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/profile"))
                .andExpect(flash().attributeExists("message"));


        Account yongbon = accountRepository.findByNickname("yongbon");
        System.out.println(yongbon.getBio());
        Assertions.assertEquals(bio, yongbon.getBio());
    }

    @WithAccount("yongbon")
    @DisplayName("프로필 수정 - 입력값 에러")
    @Test
    void updateProfile_with_wrong_input() throws Exception {
        String bio = "길게 소개를 수정하는 경우 길게 소개를 수정하는 경우 길게 소개를 수정하는 경우 길게 소개를 수정하는 경우";
        mockMvc.perform(post("/settings/profile")
                        .param("bio", bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("settings/profile"));
        Account yongbon = accountRepository.findByNickname("yongbon");
        assertNull(yongbon.getBio());
    }

    @WithAccount("yongbon")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get("/settings/password"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name("settings/password"))
                .andExpect(authenticated());
    }

    @WithAccount("yongbon")
    @DisplayName("패스워드 수정 - 입력값 정상")
    @Test
    void updatePassword_with_correct_input() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/settings/password"))
                .andExpect(flash().attributeExists("message"));

        Account yongbon = accountRepository.findByNickname("yongbon");
        assertTrue(passwordEncoder.matches("12345678", yongbon.getPassword()));
    }

    @WithAccount("yongbon")
    @DisplayName("패스워드 수정 - 입력값 에러")
    @Test
    void updatePassword_with_wrong_input() throws Exception {
        mockMvc.perform(post("/settings/password")
                        .param("newPassword", "12345678")
                        .param("newPasswordConfirm", "11111111")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().hasErrors())
                .andExpect(view().name("settings/password"));
    }
}