package com.dev.vocalab.compileRecord;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name="CompileRecord")
@Entity
@Data
@ToString
public class CompileRecordEntity {
    // DB compileRecord Table
    @Id
    // 기본키 자동 증가 설정 : GenerationType.IDENTITY
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int compileId; //일렬번호

    private String userId; //이름
    private String source; //text box에 직접 입력한 url이나 text 데이터
    private LocalDateTime createdAt; //생성일

    // insert 시 업로드 날짜 자동 입력
    @PrePersist
    protected void onPrePersist() {
        //작성일 : 현재시각으로 설정
        this.createdAt = LocalDateTime.now();
    }


}
