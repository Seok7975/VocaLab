package com.dev.vocalab.compileRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompileRecordRepository extends JpaRepository<CompileRecordEntity, Long> {
//    insert 빼고

    // home에서 표시할 compileRecord
//    List<CompileDTO> findByUserIdOrderByCreatedAtDesc(String userId);
    List<CompileRecordEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<CompileRecordEntity> findByUserId(String userId);

    CompileRecordEntity findByUserIdAndCompileId(String userId, int compileId);
}