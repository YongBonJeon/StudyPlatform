package com.studyolle.study;

import com.studyolle.account.WithAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class StudySettingsControllerTest extends StudyControllerTest {

    @Test
    @WithAccount("yongbon")
    @DisplayName("스터디 소개 수정 폼 조회 - 성공")
    void updateDescriptionForm_success() throws Exception {
        Account yongbon = accountRepository.findByNickname("yongbon");
        Study study = createStudy("test-study", yongbon);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    @Test
    @WithAccount("yongbon")
    @DisplayName("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
    void updateDescriptionForm_fail() throws Exception {
        Account whiteship = createAccount("whiteship");
        Study study = createStudy("test-study", whiteship);

        mockMvc.perform(get("/study/" + study.getPath() + "/settings/description"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAccount("yongbon")
    @DisplayName("스터디 소개 수정 - 성공")
    void updateDescription_success() throws Exception {
        Account yongbon = accountRepository.findByNickname("yongbon");
        Study study = createStudy("test-study", yongbon);

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/description")
                .param("shortDescription", "short description")
                .param("fullDescription", "full description")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/settings/description"))
                .andExpect(flash().attributeExists("message"));
    }

    @Test
    @WithAccount("yongbon")
    @DisplayName("스터디 소개 수정 - 실패")
    void updateDescription_fail() throws Exception {
        Account yongbon = accountRepository.findByNickname("yongbon");
        Study study = createStudy("test-study", yongbon);

        mockMvc.perform(post("/study/" + study.getPath() + "/settings/description")
                .param("shortDescription", "")
                .param("fullDescription", "full")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("studyDescriptionForm"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }

    private Account createAccount(String nickName) {
        Account whiteship = new Account();
        whiteship.setNickname(nickName);
        whiteship.setEmail(nickName + "@email.com");
        return accountRepository.save(whiteship);
    }

    private Study createStudy(String path, Account manager) {
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, manager);
        return study;
    }

}