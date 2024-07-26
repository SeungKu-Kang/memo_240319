package com.memo.interceptor;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component // spring bean
public class PermissionInterceptor implements HandlerInterceptor {

	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response,
			Object handler) throws IOException {
		
		// 요청 URL path를 꺼낸다.
		String uri = request.getRequestURI();		
		log.info("[@@@@@@@@@@ preHandle] uri:{}", uri);
		
		// 로그인 여부를 꺼낸다.
		HttpSession session = request.getSession();		
		Integer userId = (Integer)session.getAttribute("userId");
		
		// 비로그인 && /post => 로그인 페이지로 이동, 컨트롤러 수행 방지
		if (userId == null && uri.startsWith("/post")) {
			response.sendRedirect("/user/sign-in-view");
			return false; // 원래 요청 주소에 대한 컨트롤러 수행 X
		}
		
		// 로그인 && /user => 글목록 페이지로 이동, 컨트롤러 수행 방지
		if (userId != null && uri.startsWith("/user")) {
			response.sendRedirect("/post/post-list-view");
			return false;
		}
		
		return true; // 컨트롤러 수행 true
	}
	
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response,
			Object handler, ModelAndView mav) {
		
		// view, model 객체가 있다는 건 html이 해석되기 전 : 뭔가 더 껴넣을 수 있음
		log.info("[$$$$$$$$ postHandle]");
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response,
			Object handler, Exception ex) {
		
		// html이 완성된 상태
		log.info("[##### afterCompletion]");
	}
}
