package com.memo.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memo.user.Entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer>{

	// JPQL
	public UserEntity findByLoginId(String loginId);
	public UserEntity findByLoginIdAndPassword(String loginId, String password);
	
}
