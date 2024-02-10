package com.studyolle.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    // 유니크 제약 조건 추가
    @Column(unique = true)
    private String email;

    // 유니크 제약 조건 추가
    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDate joinedAt;

    private String bio;

    private String url;

    private String occupation;

    private String location;

    // 프로필 이미지 파일 이름
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;

    private boolean studyCreatedByWeb;

    private boolean studyEnrollmentResultByEmail;

    private boolean studyEnrollmentResultByWeb;

    private boolean studyUpdatedByEmail;

    private boolean studyUpdatedByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp(Account account) {
        account.setEmailVerified(true);
        account.setJoinedAt(LocalDate.now());
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }
}
