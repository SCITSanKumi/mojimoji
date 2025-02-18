package com.sangkeumi.mojimoji.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sangkeumi.mojimoji.dto.user.LoginUserDetails;
import com.sangkeumi.mojimoji.entity.User;
import com.sangkeumi.mojimoji.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 로그인 시도
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 username으로 User 엔티티를 찾아옴
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("ID가 존재하지 않습니다."));

        // User 엔티티를 UserDetails로 변환
        return LoginUserDetails.toDTO(user);
    }

}
