package com.dev.vocalab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, String> {

    Optional<UsersEntity> findByUserId(String userId);

    @Query("SELECT u.userId FROM UsersEntity u")
    List<String> findAllUserId();

    // OAuth2유저 판별
    UsersEntity findByUserName(String userName);

    UsersEntity findByUserEmail(String email);

    // myPage 회원정보 수정
    @Modifying
    @Transactional
    @Query("UPDATE UsersEntity u set u.userNickname = :userNickname where u.userId = :userId")
    int updateUsers(@Param("userId")String userId, @Param("userNickname")String userNickname);

    boolean existsByUserEmail(String userEmail);

}
