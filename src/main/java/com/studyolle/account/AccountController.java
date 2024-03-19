package com.studyolle.account;

import com.studyolle.domain.Account;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    /* SignUpForm 데이터를 받을 때 검증기 추가 */
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    // 회원가입 페이지
    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    // 회원가입 처리
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) { // @Valid: 검증기 실행, Errors: 검증 실패시 에러 정보

        // 검증기에서 에러 검출했을 경우 에러 메세지를 담아 다시 회원가입 페이지로 이동
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        // 회원가입 처리
        accountService.processNewAccount(signUpForm);

        // 자동 로그인 비활성화
        //accountService.login(signUpForm);

        // 초기 페이지로 이동
        return "redirect:/";
    }

    // 이메일 인증 처리
    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) { // token, email 파라미터를 받아옴
        // 이메일 파라미터로 회원 정보 조회
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        // 회원 정보가 없을 경우 에러 메세지를 담아 checked-email 페이지로 이동
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }
        // 토큰이 유효하지 않을 경우 에러 메세지를 담아 checked-email 페이지로 이동
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }
        // 이메일 인증 완료 회원가입 완료 처리
        accountService.completeSignUp(account);
        // 총 회원 수 모델에 추가
        model.addAttribute("numberOfUser", accountRepository.count());
        // 회원 이름 모델에 추가
        model.addAttribute("nickname", account.getNickname());

        // 이메일 인증 완료 View 출력
        return view;
    }

    // 이메일 재전송 페이지
    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    // 이메일 재전송 처리
    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        // 이메일 재전송 가능 여부 확인
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        // 이메일 재전송
        accountService.sendSignUpConfirmEmail(account);

        // 이메일 재전송 완료 페이지로 이동
        return "redirect:/";
    }

    // 프로필 페이지
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@CurrentUser Account account, @PathVariable String nickname, Model model) {
        // 닉네임으로 회원 정보 조회
        Account accountToView = accountService.getAccount(nickname);
        model.addAttribute(accountToView);
        // 현재 로그인한 회원과 조회한 회원이 같은 경우 isOwner 모델에 추가 (프로필 수정 버튼 표시 여부 등에 사용)
        model.addAttribute("isOwner", accountToView.equals(account));
        // 프로필 View 출력
        return "account/profile";
    }

    // 이메일로 로그인 페이지
    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }


    // 이메일로 로그인 링크 전송
    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
        // 이메일로 회원 정보 조회
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "이메일 로그인은 1시간 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }

        // 이메일로 로그인 링크 전송
        accountService.sendLoginLink(account);
        // 이메일 전송 완료 메세지를 FlashAttribute에 추가
        attributes.addFlashAttribute("message", "이메일 인증 링크를 보냈습니다.");
        return "account/email-login";
    }

    // 이메일로 로그인 처리하는 메소드지만 현재 비활성화 상태
    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if (!account.isValidToken(token)) {
            model.addAttribute("error", "유효하지 않은 토큰입니다.");
            return "account/email-login";
        }
        //accountService.login(account);
        return "redirect:/";
    }





}
