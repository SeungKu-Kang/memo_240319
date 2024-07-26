package com.memo.post.bo;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.memo.common.FileManagerService;
import com.memo.post.domain.Post;
import com.memo.post.mapper.PostMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostBO {
	// private Logger log = LoggerFactory.getLogger(PostBO.class);
	// private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PostMapper postMapper;
	
	@Autowired
	private FileManagerService fileManagerService;
	
	// 페이징 정보 필드(limit)
	private static final int POST_MAX_SIZE = 3;
	
	
	// input: 로그인된 사람의 userId
	// output: List<Post>
	public List<Post> getPostListByUserId(int userId, Integer prevId, Integer nextId) {
		// 게시글 번호 10 9 8 | 7 6 5 | 4 3 2 | 1
		// 만약 4 3 2 페이지에 있을 때
		// 1) 다음: 2보다 작은 3개를 DESC정렬
		// 2) 이전: 4보다 큰 4개를 ASC정렬 => 5 6 7 => BO에서 reverse 7 6 5
		// 3) 페이징 X: 최신순 3개 DESC정렬
		Integer standardId = null; // 기준이 되는 postId
		String direction = null; // 방향
		if (prevId != null) { // 2) 이전
			standardId = prevId;
			direction = "prev";
			
			List<Post> postList = postMapper.selectPostListByUserId(userId, standardId, direction, POST_MAX_SIZE);
			// [5 6 7] => [7 6 5]
			Collections.reverse(postList); // 리스트 뒤집고 저장
			
			return postList;
		} else if (nextId != null) { // 1) 다음
			standardId = nextId;
			direction = "next";
		}
		
		// 3) 페이징 정보 X, 1) 다음
		return postMapper.selectPostListByUserId(userId, standardId, direction, POST_MAX_SIZE);
	}
	
	// 이전 페이지의 마지막인가?
	public boolean isPrevLastPageByUserId(int userId, int prevId) {
		int maxPostId = postMapper.selectPostIdByUserIdAsSort(userId, "DESC");
		return maxPostId == prevId; // 같으면 마지막
	}
	
	
	// 다음 페이지의 마지막인가?
	public boolean isNextLastPageByUserId(int userId, int nextId) {
		int minPostId = postMapper.selectPostIdByUserIdAsSort(userId, "ASC");
		return minPostId == nextId;
	}
	
	// input : userId , postId
	// output: Post or null
	public Post getPostByPostIdUserId(int userId, int postId) {
		return postMapper.selectPostByPostIdUserId(userId, postId);
	}
	
	// input: parameters
	// output: X
	public void addPost(int userId, String userLoginId ,String subject, String content, MultipartFile file) {
		
		String imagePath = null;
		
		if (file != null) {
			// 파일에 업로드 할 이미지가 있을 때에만 업로드
			imagePath = fileManagerService.uploadFile(file, userLoginId);
			
		}
		
		postMapper.insertPost(userId, subject, content, imagePath);
	}
	
	// input: 파라미터들
	// output: X
	public void updatePostByPostId(
			int userId, String loginId, 
			int postId, String subject, 
			String content,	MultipartFile file) {
		
		// 업데이트할 기존 글을 가져온다
		// 이유 1) 이미지 교체 시 기존 글에 있던 imagePath 삭제하기 위함
		// 이유 2) 업데이트 대상이 있는지 확인하기 위함
		Post post = postMapper.selectPostByPostIdUserId(userId, postId);
		// System.out.println(); => 사용해서는 안됨 : 각 사용자는 쓰레드인데 이 코드를 적게 되면 해당 쓰레드가 서버를 점유하게 되어 서버가 느려진다.
		if (post == null) {
			log.warn("[글 수정] post is null. userId:{}, postId:{}", userId, postId); // userId가 {}안에 들어가게 된다.
			return;
		}
		
		// 파일이 있으면 
		// 1) 새 이미지를 업로드
		// 2) 1번 단계가 성공하면 기존 이미지가 있을 때 삭제
		String imagePath = null;
		
		if (file != null) {
			// 새 이미지 업로드
			imagePath = fileManagerService.uploadFile(file, loginId);
			
			// 업로드 성공 시 (null 아님) 기존 이미지가 있으면 제거 (AND조건)
			if (imagePath != null && post.getImagePath() != null) {
				// 폴더와 이미지 제거(서버에서)
				fileManagerService.deleteFile(post.getImagePath()); // ()안에 자동완성되는 것이 imagePath인데 이건 업로드할 새 이미지이므로 아님.
			}
		}
		
		// db update
		postMapper.updatePostByPostId(postId, subject, content, imagePath);
	}
	
	// input: postId, userId
	// output:X
	public void deletePostByPostIdUserId(int postId, int userId) {
		// 기존글 가져오기(이미지 존재시 삭제하기 위해)
		Post post = postMapper.selectPostByPostIdUserId(userId, postId);
		if (post == null) {
			log.info("[글 삭제] post is null. postId:{}, userId:{}", postId, userId);
			return;
		}
		
		// post 먼저 db delete
		int rowCount = postMapper.deletePostByPostId(postId);
		
		// 이미지가 존재하면 삭제, 삭제된 행도 1개 일 때 
		if (rowCount > 0 && post.getImagePath() != null) {
			fileManagerService.deleteFile(post.getImagePath());
			
		}
	}
}















