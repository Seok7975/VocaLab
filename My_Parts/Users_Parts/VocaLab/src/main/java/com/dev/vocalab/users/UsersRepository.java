package com.dev.vocalab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<UsersEntity, Long> {

    Optional<UsersEntity> findByUserId(String userId);

    @Query("SELECT u.userId FROM UsersEntity u")
    List<String> findAllUserId();
    //List<String> findUserIdBy();  // = @Query("SELECT u.userId FROM User m")
    //List<String> findDistinctUserIdBy();

    // OAuth2유저 판별
    UsersEntity findByUserName(String userName);

    UsersEntity findByUserEmail(String email);

    boolean existsByUserEmail(String userEmail);
}
