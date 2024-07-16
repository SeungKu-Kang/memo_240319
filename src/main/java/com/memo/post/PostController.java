package com.memo.post;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.memo.post.bo.PostBO;
import com.memo.post.domain.Post;

import jakarta.servlet.http.HttpSession;

@RequestMapping("/post")
@Controller
public class PostController {

	@Autowired
	private PostBO postBO;
	
	
	@GetMapping("/post-list-view")
	public String postListView(HttpSession session, Model model) {
		// 로그인 여부 확인
		Integer userId = (Integer)session.getAttribute("userId"); // null이 들어올 수 있기 때문에 int가 아닌 Integer로 다운캐스팅
		if (userId == null) {
			// 비로그인이면(=null) 로그인 페이지로 이동
			return "redirect:/user/sign-in-view";
		}
		
		// DB 조회 - 글 목록
		List<Post> postList = postBO.getPostListByUserId(userId);
		
		// 모델에 담기
		model.addAttribute("postList", postList);
		
		return "post/postList";
	}
	
	/**
	 * 글쓰기 화면
	 * @return
	 */
	@GetMapping("/post-create-view")
	public String postCreateView() {
		return "post/postCreate";
	}
}
