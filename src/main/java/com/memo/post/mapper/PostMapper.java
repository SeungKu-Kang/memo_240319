package com.memo.post.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.multipart.MultipartFile;

import com.memo.post.domain.Post;

@Mapper
public interface PostMapper {

	public List<Map<String, Object>> selectPostListTest();
	
	public List<Post> selectPostListByUserId(int userId);
	
	public Post insertPost(String subject, String content, MultipartFile file);
}
