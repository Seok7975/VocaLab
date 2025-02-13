package com.dev.vocalab.home;

import com.dev.vocalab.compileRecord.CompileDTO;
import com.dev.vocalab.compileRecord.CompileRecordRepository;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.dev.vocalab.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final CompileRecordRepository compileRecordRepository;
    private final FilesRepository filesRepository;
    private final UsersRepository usersRepository;

//    // home에서 compileRecord 표시
//    public List<CompileDTO> selectRecord(String userId) {
//        List<CompileDTO> dtoList = compileRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
//        List<FilesEntity> filesList = filesRepository.findByUserId(userId);
//        for (CompileDTO dto : dtoList) {
//
//            for (FilesEntity file : filesList) {
//                // CompileId와 TableId가 같으면 fileRecordList에 추가
//                if (dto.getCompileId() == file.getTableId()) {
//                    dto.getFileRecordList().add(file); // 매칭된 파일 추가
//                }
//            }
//        }
//        System.out.println(dtoList);
//
//        return dtoList;
//    }


//    public UsersEntity selectUser(String userId) {
//        return usersRepository.findByUserId(userId);
//    }
}
