package com.studyolle.account;

import com.studyolle.mail.EmailMessage;
import com.studyolle.mail.EmailService;
import com.studyolle.config.AppProperties;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.domain.Account;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;


    public void processNewAccount(SignUpForm signUpForm) {
        // 회원가입 처리
        Account newAccount = saveNewAccount(signUpForm);

        // 이메일 인증 토큰 생성
        newAccount.generateEmailCheckToken();

        // 이메일 전송
        sendSignUpConfirmEmail(newAccount);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        // 타임리프 템플릿 엔진을 사용하여 이메일 내용 생성
        String message = templateEngine.process("mail/single-link", context);

        // 이메일 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail()) // 받는 사람
                .subject("스터디올래, 회원 가입 인증") // 이메일 제목
                .message(message) // 이메일 내용
                .build();

        // 이메일 전송
        emailService.sendEmail(emailMessage);
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        // Spring에서 제공하는 비밀번호 암호화
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));

        // ModelMapper를 사용하여 SignUpForm을 Account로 변환
        Account account = modelMapper.map(signUpForm, Account.class);

        // Account 저장
        return accountRepository.save(account);
    }

    // Spring Security에서 제공하는 UserDetailsService를 구현
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        // 이메일 또는 닉네임으로 회원 정보 조회
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        // UserDetails 인터페이스를 구현한 UserAccount 객체 생성
        return new UserAccount(account);
    }

    // 이메일 인증 완료 처리
    public void completeSignUp(Account account) {
        account.setEmailVerified(true);
        account.setJoinedAt(LocalDate.now());
    }

    // 프로필 업데이트 처리
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account);
        accountRepository.save(account);
    }

    // 비밀번호 변경 처리
    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    // 알림 설정 업데이트 처리
    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
        accountRepository.save(account);
    }

    // 닉네임 변경 처리
    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
    }

    // 비밀번호를 모를 때 이메일로 로그인 링크 전송
    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();

        account.generateEmailCheckToken();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getZones();
    }

    public void removeZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getZones().remove(zone));
    }

    public void addZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getZones().add(zone));
    }

    public Account getAccount(String nickname) {
        Account byNickname = accountRepository.findByNickname(nickname);
        if (nickname == null) {
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }
        return byNickname;
    }
}
