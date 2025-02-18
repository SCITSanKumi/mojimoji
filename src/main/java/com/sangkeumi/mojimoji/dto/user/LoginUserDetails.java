package com.sangkeumi.mojimoji.dto.user;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sangkeumi.mojimoji.entity.User;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class LoginUserDetails implements UserDetails, MyPrincipal {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String role;

    // ROLE의 목록을 만들어서 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    // 실제 패스워드로 사용하는 변수
    @Override
    public String getPassword() {
        return this.password;
    }

    // 실제 ID로 사용하는 변수
    @Override
    public String getUsername() {
        return this.username;
    }

    // 닉네임
    public String getNickname() {
        return this.nickname;
    }

    @Override
    public Long getUserId() {
        return this.userId;
    }

    public static LoginUserDetails toDTO(User user) {
        return LoginUserDetails.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

}
