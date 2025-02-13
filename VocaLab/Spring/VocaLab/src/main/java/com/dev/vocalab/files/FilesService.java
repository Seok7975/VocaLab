package com.dev.vocalab.files;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class FilesService {
    private final FilesRepository filesRepository;
//    private final String realPath = "C:/Dev/Data/VocaLab/Spring/VocaLab/src/main/resources/static/images/upload";
    @Value("${file.upload-dir}")
    private String uploadDir;  // `/home/files` 경로 설정

    // [📌] 변경된 이미지 저장 경로
    private String getBoardImagePath(Integer boardId) {
        return uploadDir + "/images/upload/board/" + boardId;
    }

    @Autowired
    public FilesService(FilesRepository filesRepository) {
        this.filesRepository = filesRepository;
    }

    // 파일 업로드 처리 - Summernote에서 호출
    public JSONObject uploadFile(MultipartFile multipartFile, String tempPath) {
        JSONObject jsonObject = new JSONObject();
        log.info("FileService.uploadFile 시작: {}", multipartFile.getOriginalFilename());

        try {
            // 디렉토리 생성
            File directory = new File(tempPath);
            if (!directory.exists()) {
                directory.mkdirs();
                log.info("디렉토리 생성: {}", tempPath);
            }

            // 파일명 생성
            String originalFileName = validateAndGetFileName(multipartFile);
            String savedFileName = generateUniqueFileName(originalFileName);

            // 파일 저장 경로 생성
            File targetFile = new File(tempPath + File.separator + savedFileName);

            // 파일 저장
            multipartFile.transferTo(targetFile);
            log.info("파일 저장 완료: {}", targetFile.getAbsolutePath());

            jsonObject.put("url", "/images/upload/board/temp/" + savedFileName);
            jsonObject.put("responseCode", "success");

        } catch (Exception e) {
            log.error("파일 업로드 실패", e);
            jsonObject.put("responseCode", "error");
            jsonObject.put("message", e.getMessage());
        }

        return jsonObject;
    }

    // 폴더 간 파일 복사
    public void copyFiles(String sourceFolder, String targetFolder) {
        try {
            File source = new File(sourceFolder);
            File target = new File(targetFolder);

            createDirectoryIfNotExists(source);
            createDirectoryIfNotExists(target);

            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyFile(file, target);
                }
            }
        } catch (IOException e) {
            log.error("File copy failed", e);
            throw new RuntimeException("파일 복사 중 오류가 발생했습니다.", e);
        }
    }

    // 폴더 삭제
    public void deleteFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            log.info("Folder does not exist: {}", path);
            return;
        }

        try {
            deleteFilesInFolder(folder);
            if (!folder.delete()) {
                log.warn("Failed to delete folder: {}", folder.getPath());
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting folder: {}", path, e);
            throw new RuntimeException("폴더 삭제 중 오류가 발생했습니다.", e);
        }
    }

    // 게시글 관련 모든 파일 삭제
    @Transactional
    public void deleteAllBoardFiles(Integer boardId) {
        List<FilesEntity> files = filesRepository.findByTableIdAndCategory(boardId, FilesEntity.Category.BOARD);
        filesRepository.deleteAll(files);
    }

    // 더미 파일 삭제 (본문에 포함되지 않은 파일들)
    public void removeDummyFiles(String folderPath, String content) {
        List<String> fileNames = getFileNamesFromFolder(folderPath);
        for (String fileName : fileNames) {
            if (!content.contains(fileName)) {
                deleteFile(folderPath + "/" + fileName);
            }
        }
    }

    private String validateAndGetFileName(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 비어있습니다.");
        }
        return fileName;
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return UUID.randomUUID() + extension;
    }

    private File saveFile(MultipartFile file, String path, String fileName) throws IOException {
        File targetFile = new File(path + fileName);
        FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);
        return targetFile;
    }

    private void createDirectoryIfNotExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void copyFile(File source, File targetDir) throws IOException {
        File target = new File(targetDir, source.getName());
        if (source.isFile()) {
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(target)) {
                FileChannel sourceChannel = fis.getChannel();
                FileChannel targetChannel = fos.getChannel();
                sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
            }
        }
    }

    private void deleteFilesInFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.canWrite()) {
                        file.setWritable(true);
                    }
                    if (!file.delete()) {
                        log.warn("Failed to delete file: {}", file.getName());
                    }
                } else {
                    deleteFolder(file.getPath());
                }
            }
        }
    }

    private List<String> getFileNamesFromFolder(String folderPath) {
        File folder = new File(folderPath);
        List<String> fileNames = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            log.warn("Failed to delete file: {}", filePath);
        }
    }
    @Transactional
    public void handleBoardFileUpload(Integer boardId, MultipartFile thumbnail, String userId) {
        String tempPath = uploadDir + "/images/upload/board/temp/";
        String uploadPath = getBoardImagePath(boardId);

        copyFiles(tempPath, uploadPath);
        deleteFolder(tempPath);

        createBoardFileEntities(uploadPath, boardId, thumbnail, userId);
    }

    // 게시글 파일 업데이트 처리
    @Transactional
    public void handleBoardFileUpdate(Integer boardId, String content, MultipartFile thumbnail, String userId) {
        String tempPath = uploadDir + "/images/upload/board/temp/";
        String boardPath = getBoardImagePath(boardId);

        try {
            // 1. 기존 파일 엔티티들 삭제
            filesRepository.deleteByTableIdAndCategory(boardId, FilesEntity.Category.BOARD);

            // 2. DB 변경사항을 즉시 반영
            filesRepository.flush();

            // 3. 파일 시스템 작업 수행
            removeDummyFiles(tempPath, content);
            deleteFolder(boardPath);
            copyFiles(tempPath, boardPath);

            // 4. 새 파일 엔티티 생성 및 저장
            File uploadDir = new File(boardPath);
            File[] files = uploadDir.listFiles();
            if (files != null) {
                String thumbnailFilename = thumbnail != null ? thumbnail.getOriginalFilename() : null;
                for (File file : files) {
                    if (file.isFile() && (thumbnailFilename == null ||
                            !file.getName().equals(thumbnailFilename))) {
                        FilesEntity filesEntity = FilesEntity.builder()
                                .category(FilesEntity.Category.BOARD)
                                .filePath("/images/upload/board/" + boardId + "/" + file.getName())
                                .fileType(FilesEntity.FileType.IMAGE)
                                .uploadedAt(LocalDateTime.now())
                                .tableId(boardId)
                                .userId(userId)
                                .build();
                        filesRepository.saveAndFlush(filesEntity);
                    }
                }
            }
        } catch (Exception e) {
            log.error("파일 업데이트 중 오류 발생: ", e);
            throw new RuntimeException("파일 업데이트 실패", e);
        }
    }

    // 게시글 파일 엔티티 생성
    private void createBoardFileEntities(String uploadPath, Integer boardId,
                                         MultipartFile thumbnail, String userId) {
        File uploadDir = new File(uploadPath);
        File[] files = uploadDir.listFiles();
        if (files != null) {
            String thumbnailFilename = thumbnail != null ? thumbnail.getOriginalFilename() : null;
            for (File file : files) {
                if (file.isFile() && (thumbnailFilename == null ||
                        !file.getName().equals(thumbnailFilename))) {
                    FilesEntity filesEntity = FilesEntity.builder()
                            .category(FilesEntity.Category.BOARD)
                            .filePath("/images/upload/board/" + boardId + "/" + file.getName())
                            .fileType(FilesEntity.FileType.IMAGE)
                            .uploadedAt(LocalDateTime.now())
                            .tableId(boardId)
                            .userId(userId)
                            .build();
                    filesRepository.save(filesEntity);
                    log.info("Created file entity: {}", filesEntity.getFilePath());
                }
            }
        }
    }
}
