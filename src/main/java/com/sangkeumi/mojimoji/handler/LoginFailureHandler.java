package com.sangkeumi.mojimoji.handler;

import java.io.IOException;
import java.net.URLEncoder;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    log.info("LoginFailureHandler : 로그인 실패");
    String errMessage = "";

    if (exception instanceof BadCredentialsException) {
      errMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";
    } else {
      errMessage = exception + exception.getMessage();
      errMessage = "로그인에 실패했습니다. 다시 시도해 주세요";
    }
    errMessage = URLEncoder.encode(errMessage, "UTF-8");

    this.setDefaultFailureUrl("/user/login?error=true&message=" + errMessage);
    super.onAuthenticationFailure(request, response, exception);
  }

}
