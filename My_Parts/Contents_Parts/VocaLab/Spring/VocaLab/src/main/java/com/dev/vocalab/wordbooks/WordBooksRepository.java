package com.dev.vocalab.wordbooks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordBooksRepository extends JpaRepository<WordBooksEntity, Integer> {

    // 사용자별 WordBook 조회
    List<WordBooksEntity> findByUserId(String userId);

    // 북마크된 WordBook 조회
    List<WordBooksEntity> findByBookmarkTrue();

    // 제목으로 WordBook 검색
    List<WordBooksEntity> findByWordBookTitleContainingIgnoreCase(String wordBookTitle);

    WordBooksEntity findByWordBookId(Integer wordBookId);
}
