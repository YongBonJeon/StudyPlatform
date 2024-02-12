package com.studyolle.account;

import com.studyolle.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

// 유저가 사용하는 계정 정보와 스프링 세큐리티가 사용하는 유저 정보를 연결해주는 클래스
@Getter
public class UserAccount extends User {

        private Account account;

        public UserAccount(Account account) {
            super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
            this.account = account;
        }



}
