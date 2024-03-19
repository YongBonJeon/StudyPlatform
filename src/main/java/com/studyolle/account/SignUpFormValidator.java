package com.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {     // Spring MVC에서 제공하는 Validator 인터페이스를 구현하여 사용자가 입력한 데이터를 검증하는 기능을 구현

    private final AccountRepository accountRepository;
    @Override
    public boolean supports(Class<?> aClass) {
        return SignUpForm.class.isAssignableFrom(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) o;

        // 중복 이메일 사용불가 요구사항 확인
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }
        // 중복 닉네임 사용불가 요구사항 확인
        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 닉네임입니다.");
        }
    }
}
