package com.dev.vocalab.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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


}
