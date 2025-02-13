package com.dev.vocalab.myPage;

import com.dev.vocalab.compileRecord.CompileDTO;
import com.dev.vocalab.compileRecord.CompileRecordEntity;
import com.dev.vocalab.compileRecord.CompileRecordRepository;
import com.dev.vocalab.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {


    private final UsersRepository usersRepository;
    private final CompileRecordRepository compileRecordRepository;


    // [ myPageUserModifyPro - 회원정보수정(닉네임수정)]
    public void modifyNickName(String userId, String nickName) {
        System.out.println("MyPageService - modifyNickName");
        System.out.println("userId: " + userId);
        System.out.println("nickName: " + nickName);
        int updatedRows = usersRepository.updateUsers(userId, nickName);
        if (updatedRows == 0) {
            throw new RuntimeException("닉네임 변경 실패: 해당 사용자 없음");
        }
    }



    // [ myPageRecord - 단어기록 ]
    public List<CompileDTO> getCompileRecordList(String userId) {
        System.out.println("MyPageService - getCompileRecordList");

        List<CompileRecordEntity> recordList = compileRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("recordList: " + recordList);

        // < 일자 계산 >
        List<CompileDTO> comList = new ArrayList<>();
        for (CompileRecordEntity record : recordList) {
            LocalDateTime now = LocalDateTime.now(); // 현재 날짜 및 시간
            LocalDateTime recordDateTime = record.getCreatedAt(); // 기록 시간

            System.out.println("recordDateTime: " + recordDateTime);
            long hoursBetween = Duration.between(recordDateTime, now).toHours();
            System.out.println("hoursBetween: " + hoursBetween);
            long daysBetween = hoursBetween / 24;
            System.out.println("daysBetween: " + daysBetween);

            System.out.println("record : " + record);

            CompileDTO com = new CompileDTO();
            com.setCompileId(record.getCompileId());
            com.setUserId(record.getUserId());
            com.setSource(record.getSource());
            com.setCreatedAt(record.getCreatedAt());
            com.setDaysAgo(daysBetween);
            com.setHoursAgo(hoursBetween);
            System.out.println("com : " + com);

            comList.add(com);

        }
        System.out.println("comList: " + comList);
        return comList;
    }

}
