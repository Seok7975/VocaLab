package com.dev.vocalab.compileRecord;

import com.dev.vocalab.files.FileHandler;
import com.dev.vocalab.files.FilesDTO;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
// 생성자를 통한 의존성 투입
@RequiredArgsConstructor
public class CompileService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CompileRecordRepository compileRecordRepository;
    private final FilesRepository filesRepository;
    private final FileHandler fileHandler;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // [ compilePro ]
    @Transactional
    public List<Map<String, Object>> compileProService(CompileRecordEntity com, FilesEntity files, List<MultipartFile> multipartFiles, String userId) {
        System.out.println("compileProService");
        System.out.println("com: " + com.getCompileId());
        try {
            List<FilesDTO> filesDto = new ArrayList<>();
            FilesDTO fileDto;
            // 이미 기록 되어 있다면 compileRecord, Files insert 생략
            if(com.getCompileId() == 0){
                // < 1. CompileRecord INSERT > - 단어 추출 기록
                com = compileRecordRepository.save(com);
                System.out.println("insert result com : " + com);

                // < 2 원본 파일이 있다면 처리 후 저장, 없다면 넘어가기 > - 원본 파일 저장
                for (MultipartFile multipartFile : multipartFiles) {
                    System.out.println("for - file : " + multipartFile.getOriginalFilename());

                    // 1. FilesDTO 설정
                    fileDto = new FilesDTO();
                    fileDto.setUserId(com.getUserId());
                    fileDto.setCompileId(Integer.toString(com.getCompileId()));
                    fileDto.setFile(multipartFile);
                    fileDto.setOriginalFileName(multipartFile.getOriginalFilename());

                    // 2. 경로 설정
                    String compilePath = Paths.get(uploadDir, "uploads", "compileRecord").toString();
                    String saveDir = Paths.get(compilePath, userId).toString();
                    fileDto.setSaveDir(saveDir);
                    String originalFileName = fileDto.getOriginalFileName();
                    System.out.println("originalFileName : " + originalFileName);
                    System.out.println("saveDirFinal : " + saveDir);

                    if (!originalFileName.isEmpty()) {
                        // 3. 파일 타입 설정
//                        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
                        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".")); // 점(.)을 포함
                        String fileExtensionWithoutDot = fileExtension.substring(1); // 점(.)을 제외
                        if (fileExtensionWithoutDot.equalsIgnoreCase("jpg") ||
                                fileExtensionWithoutDot.equalsIgnoreCase("jpeg") ||
                                fileExtensionWithoutDot.equalsIgnoreCase("png")) {
                            fileDto.setFileType("IMAGE");
                        } else {
                            fileDto.setFileType("FILE");
                        }

                        // 4. 디렉토리 생성 및 권한 설정
                        File uploadDirectory = new File(saveDir);
                        if (!uploadDirectory.exists()) {
                            uploadDirectory.mkdirs();
                            uploadDirectory.setReadable(true, false);
                            uploadDirectory.setWritable(true, false);
                            uploadDirectory.setExecutable(true, false);
                        }
                        System.out.println("디렉토리 생성 및 권한 설정 완료");

                        // 5. UUID 파일명 생성
//                        String dbPath = saveDir.replace(uploadDir, "").replaceFirst("^/", "");
////                        String uuid = UUID.randomUUID().toString().substring(0, 8);
////                        String newFileName = uuid + "_" + originalFileName;
//                        String newFileName = originalFileName;
                        String dbPath = saveDir.replace(uploadDir, "").replaceFirst("^/", "");
                        String uuid = UUID.randomUUID().toString();
                        String newFileName = uuid + fileExtension;

                        fileDto.setOriginalFileName(newFileName);

                        // 6. 새 파일명으로 파일 저장 및 권한 설정
                        File destinationFile = new File(saveDir, newFileName);
                        multipartFile.transferTo(destinationFile);
                        destinationFile.setReadable(true, false);
                        destinationFile.setWritable(true, false);
                        destinationFile.setExecutable(true, false);
                        System.out.println("파일 저장 및 권한 설정 완료");

                        // 7. Files 엔티티 설정 및 저장
                        files.setUserId(com.getUserId());
                        files.setCategory(FilesEntity.Category.COMPILE);
                        files.setFilePath(dbPath + File.separator + newFileName);
                        files.setFileType(FilesEntity.FileType.valueOf(fileDto.getFileType()));
                        files.setUploadedAt(com.getCreatedAt());
                        files.setTableId(com.getCompileId());
                        System.out.println("원본 파일 저장 완료 files : " + files);

                        files = filesRepository.saveAndFlush(files);
                        System.out.println("DB 저장 결과 - files: " + files);

                        // 8. Python 처리를 위한 DTO 리스트에 추가
                        filesDto.add(fileDto);
                        System.out.println("filesDto : " + filesDto);
                    }
                }
                System.out.println("for문 나옴");

            } else {
                // compileId가 존재하면 해당기록 불러오기
                System.out.println("mypage에서 기록으로 단어 추출하기");
                com = compileRecordRepository.findByUserIdAndCompileId(userId, com.getCompileId());
                List<FilesEntity> filesList = filesRepository.findByTableIdAndCategoryAndUserId(com.getCompileId(), FilesEntity.Category.COMPILE, userId);

                for (FilesEntity file : filesList) {
                    // uploadDir + 상대경로로 전체 경로 생성
                    String saveDir = File.separator +file.getFilePath().split(userId)[0]+userId;
                    System.out.println("saveDir : " + saveDir);

                    // 파일명 추출 (경로의 마지막 부분)
                    String originalFileName = file.getFilePath().substring(file.getFilePath().lastIndexOf(File.separator) + 1);
                    System.out.println("originalFileName : " + originalFileName);

                    // 파일 확장자 추출
                    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
                    System.out.println("fileExtension : " + fileExtension);

                    // FilesDTO 생성 및 리스트에 추가
                    fileDto = new FilesDTO(saveDir, originalFileName, fileExtension, "");
                    filesDto.add(fileDto);

                    System.out.println("Added to filesDto - saveDir: " + saveDir +
                            ", originalFileName: " + originalFileName +
                            ", fileExtension: " + fileExtension);
                }
                System.out.println("Final filesDto : " + filesDto);
            }

            // < 3. python 처리 >
            System.out.println("comSouce : " + com.getSource());
            List<Map<String, Object>> compileWordsJson = fileHandler.AICompileWords(com.getSource(), filesDto);
            System.out.println("compileWordsJson : " + compileWordsJson);

            // < 4. 결과 CSV 파일 저장 >
            fileDto = new FilesDTO(com.getUserId(), Integer.toString(com.getCompileId()),
                    "compileResult_" + com.getCompileId() + ".csv");

            System.out.println("compileResult_fileDto : " + fileDto);
            String compilePath = Paths.get(uploadDir, "compileRecord").toString();
            String saveDir = Paths.get(compilePath, userId).toString();
            fileDto.setSaveDir(saveDir);
            String saveCsv = Paths.get(saveDir, fileDto.getOriginalFileName()).toString();
            System.out.println("compileResult_saveDirFinal : " + saveDir);

            File resultDirectory = new File(saveDir);
            if (!resultDirectory.exists()) {
                resultDirectory.mkdirs();
                resultDirectory.setReadable(true, false);
                resultDirectory.setWritable(true, false);
                resultDirectory.setExecutable(true, false);
            }
            List<Map<String, Object>> wordList = fileHandler.saveResultCSV(compileWordsJson, saveCsv);

            return wordList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("파일 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    // [ 단어 추출 기록 중 원본 파일만 삭제됨 ] : 미완
    @Transactional
    @Scheduled(cron = "0 0 12 * * *") // 오후 12시에 메서드 동작
    public void removeOriginalFiles() {
//        try {
            System.out.println("removeCompileRecords");
            // 파일 서버 디렉토리 찾아서 삭제
            // 디렉토리 경로 찾아오기
//            List<CompileFilesEntity> filelist = filesRepository.fineBYUploadedAtAndCategory(LocalDateTime.now().minusDays(7), "COMPILE");

//            for(CompileFilesEntity file : filelist) {
//
//                // db의 파일경로
//                String dbFilePath = file.getFilePath();
//                System.out.println("dbFilePath : " + dbFilePath);
//                // comfileId기준으로 삭제
//                String comfileId = Integer.toString(file.getTableId());
//
//                int idx = dbFilePath.indexOf(comfileId);
//
//                // 삭제할 진짜 경로
//                String dir = dbFilePath.split(idx + 1);
//
//                File deleteDir = new File(dir);
//
//                // 삭제할 디렉토리 안 원본파일 삭제
//                File[] deleteDirList = deleteDir.listFiles();
//                for (File deleteDirFile : deleteDirList) {
//                    deleteDirFile.delete();
//                }
//                // 디렉토리 삭제
//                deleteDir.delete();


                //생성일기준 7일 지난 원본 파일 삭제
                // [ Files Delete ]
//                filesRepository.deleteByFileId(file.getFileId());
//            }
//
//
//        } catch (RuntimeException e) {
//            throw new RuntimeException(e);
//        }
    }



} //CompileService