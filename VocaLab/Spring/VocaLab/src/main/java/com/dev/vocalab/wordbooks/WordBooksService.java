package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FileHandler;
import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WordBooksService {

    private final FilesRepository filesRepository;
    private final WordBooksRepository wordBooksRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<String[]> readWordBook(Integer wordBookId) throws Exception {
        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD)
                .stream()
                .findFirst();

        if (fileEntityOpt.isEmpty()) {
            throw new Exception("No file found for the given wordBookId.");
        }

        String relativeFilePath = fileEntityOpt.get().getFilePath();
        // 상대 경로에서 불필요한 'uploads/wordBooks' 제거 (이미 포함되어 있을 수 있음)
        relativeFilePath = relativeFilePath.replace("uploads/wordBooks/", "");
        relativeFilePath = relativeFilePath.replace("uploads\\wordBooks\\", "");

        // 절대 경로 구성
        String absoluteFilePath = Paths.get(uploadDir, "uploads", "wordBooks", relativeFilePath).normalize().toString();

        if (!Files.exists(Paths.get(absoluteFilePath))) {
            throw new Exception("The file does not exist: " + absoluteFilePath);
        }

        List<String[]> wordList = new ArrayList<>();

        // UTF-8 with BOM 인코딩으로 파일 읽기
        try (InputStream is = new FileInputStream(absoluteFilePath);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(isr)) {

            // BOM 확인 및 스킵
            is.mark(4);
            byte[] bom = new byte[3];
            if (is.read(bom) == 3 && bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                // BOM이 있는 경우 스킵됨
            } else {
                is.reset(); // BOM이 없으면 처음으로 돌아감
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                // 빈 라인 스킵
                if (line.length > 0 && (line[0].trim().length() > 0 || (line.length > 1 && line[1].trim().length() > 0))) {
                    wordList.add(line);
                }
            }
        } catch (IOException e) {
            throw new Exception("Failed to read the CSV file: " + absoluteFilePath, e);
        }

        return wordList;
    }

    public void deleteWord(Integer wordBookId, List<String> deleteWordList) throws Exception {
        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD)
                .stream()
                .findFirst();

        if (fileEntityOpt.isEmpty()) {
            throw new Exception("No file found for the given wordBookId.");
        }

        String relativeFilePath = fileEntityOpt.get().getFilePath();
        String absoluteFilePath = Paths.get(uploadDir, "wordbooks", relativeFilePath).toString();

        if (!Files.exists(Paths.get(absoluteFilePath))) {
            throw new Exception("The file does not exist: " + absoluteFilePath);
        }

        List<String[]> remainingWords = new ArrayList<>();

        // 기존 파일 읽기
        try (InputStream is = new FileInputStream(absoluteFilePath);
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(isr)) {

            // BOM 스킵
            is.mark(4);
            byte[] bom = new byte[3];
            if (is.read(bom) == 3 && bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                // BOM이 있으면 스킵됨
            } else {
                is.reset();
            }

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length > 0 && !deleteWordList.contains(line[0])) {
                    remainingWords.add(line);
                }
            }
        }

        // 파일 다시 쓰기 (UTF-8 BOM 포함)
        try (FileOutputStream fos = new FileOutputStream(absoluteFilePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            // Write UTF-8 BOM
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            // Write content
            try (CSVWriter csvWriter = new CSVWriter(writer)) {
                csvWriter.writeAll(remainingWords);
            }
        }
    }

    @Transactional
    public void addWords(String userId, int wordBookId, List<String> words, List<String> meanings) {
        System.out.println("wordBookService - addWords");

        WordBooksEntity wordBook = wordBooksRepository.findByWordBookId(wordBookId);
        String wordBookTitle = wordBook.getWordBookTitle();
        FilesEntity csvfiles = filesRepository.findByUserIdAndTableIdAndCategory(userId, wordBookId, FilesEntity.Category.WORD);

        // 파일명 영문화 및 특수문자 제거
        String safeTitleName = getSafeFileName(wordBookTitle);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String safeFileName = uuid + "_" + safeTitleName + ".csv";

        // 경로 정규화
        Path baseDir = Paths.get(uploadDir, "uploads", "wordBooks").normalize();
        Path userDir = baseDir.resolve(userId).normalize();
        Path filePath = userDir.resolve(safeFileName).normalize();

        try {
            // 디렉토리 생성
            Files.createDirectories(userDir);

            // CSV 파일이 없는 경우 (새로 추가)
            if (csvfiles == null) {
                handleNewCsvFile(userId, wordBookId, words, meanings, userDir.toString(), safeFileName);
            } else {
                // 기존 CSV 파일에 추가
                updateExistingCsvFile(words, meanings, filePath.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory or process file: " + e.getMessage(), e);
        }
    }

    private String getSafeFileName(String fileName) {
        // 파일명에서 확장자 제거
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }

        // 영문, 숫자, 하이픈, 언더스코어만 허용
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9-_]", "_");

        // 중복된 특수문자 제거
        safeFileName = safeFileName.replaceAll("_+", "_");

        // 시작과 끝의 특수문자 제거
        safeFileName = safeFileName.replaceAll("^_+|_+$", "");

        // 비어있는 경우 기본값 사용
        if (safeFileName.isEmpty()) {
            safeFileName = "wordbook";
        }

        return safeFileName;
    }

    private void handleNewCsvFile(String userId, int wordBookId, List<String> words, List<String> meanings,
                                  String saveDir, String originalFileName) {
        if (words == null || meanings == null || words.size() != meanings.size()) {
            throw new IllegalArgumentException("단어와 뜻의 개수가 맞지 않거나 데이터가 비어있습니다.");
        }

        Path filePath = Paths.get(saveDir, originalFileName);

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            // Write UTF-8 BOM
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                String meaning = processMeaning(meanings.get(i));

                writer.write(String.format("\"%s\",\"%s\"%n", word, meaning));
            }

            // DB에 파일 정보 저장
            String relativePath = Paths.get("uploads", "wordBooks", userId, originalFileName)
                    .normalize()
                    .toString()
                    .replace('\\', '/');  // Windows 경로 구분자를 Unix 스타일로 변환

            saveFileInformation(userId, wordBookId, relativePath);

        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 생성 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private void saveFileInformation(String userId, int wordBookId, String relativePath) {
        FilesEntity csvfiles = new FilesEntity();
        csvfiles.setUserId(userId);
        csvfiles.setCategory(FilesEntity.Category.WORD);
        csvfiles.setFilePath(relativePath);
        csvfiles.setFileType(FilesEntity.FileType.FILE);
        csvfiles.setTableId(wordBookId);

        filesRepository.save(csvfiles);
    }

    private String sanitizeFileName(String fileName) {
        // 파일명에서 확장자 제거
        int extensionIndex = fileName.lastIndexOf(".");
        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }

        // 허용되는 문자만 유지 (영문, 숫자, 한글, 공백)
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9가-힣\\s-]", "_");

        // 공백을 언더스코어로 변경
        sanitized = sanitized.replaceAll("\\s+", "_");

        // 중복된 언더스코어 제거
        sanitized = sanitized.replaceAll("_+", "_");

        // 시작과 끝의 언더스코어 제거
        sanitized = sanitized.replaceAll("^_+|_+$", "");

        // UUID 추가하여 고유성 보장
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return uuid + "_" + sanitized;
    }

    private void updateExistingCsvFile(List<String> words, List<String> meanings, String filePath) {
        if (words == null || meanings == null || words.size() != meanings.size()) {
            throw new IllegalArgumentException("단어와 뜻의 개수가 맞지 않거나 데이터가 비어있습니다.");
        }

        List<String[]> wordList = new ArrayList<>();

        // Read existing content with UTF-8
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8);
             CSVReader reader = new CSVReader(isr)) {

            String[] line;
            while ((line = reader.readNext()) != null) {
                wordList.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("기존 CSV 파일 읽기 중 오류 발생: " + e.getMessage(), e);
        }

        // Add new words (checking for duplicates)
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            String meaning = processMeaning(meanings.get(i));

            boolean isDuplicate = wordList.stream()
                    .anyMatch(row -> row[0].equals(word) && row[1].equals(meaning));

            if (!isDuplicate) {
                wordList.add(new String[]{word, meaning});
            }
        }

        // Check word limit
        if (wordList.size() > 200) {
            throw new IllegalArgumentException("단어장은 최대 200개의 단어만 허용됩니다.");
        }

        // Write back with UTF-8 BOM
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {

            // Write UTF-8 BOM
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            // Write content
            for (String[] line : wordList) {
                writer.write(String.format("\"%s\",\"%s\"%n", line[0], line[1]));
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV 파일 업데이트 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private String processMeaning(String meaning) {
        String[] parts = meaning.split("\\.");
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return meaning.trim();
    }

    private void saveFileInformation(String userId, int wordBookId, String saveDir, String originalFileName) {
        String[] dir = saveDir.split("static/");
        String dbPath = dir.length > 1 ? dir[1] : saveDir;
        String dbFilePath = FileHandler.renameFile(dbPath, originalFileName);

        FilesEntity csvfiles = new FilesEntity();
        csvfiles.setUserId(userId);
        csvfiles.setCategory(FilesEntity.Category.WORD);
        csvfiles.setFilePath(dbFilePath);
        csvfiles.setFileType(FilesEntity.FileType.FILE);
        csvfiles.setTableId(wordBookId);

        filesRepository.save(csvfiles);
    }

    public void changeStatusOfBookmark(Integer wordBookId) {
        WordBooksEntity wordBooks = wordBooksRepository.findByWordBookId(wordBookId);
        System.out.println("Before set: " + wordBooks.getBookmark());
        wordBooks.setBookmark(!wordBooks.getBookmark());
        System.out.println("After set: " + wordBooks.getBookmark());
        wordBooksRepository.saveAndFlush(wordBooks);
    }

    public WordBooksEntity loadWordBookStatus(Integer wordBookId) {
        return wordBooksRepository.findByWordBookId(wordBookId);
    }

    public void updateWordBookTitle(Integer wordBookId, String newTitle) {
        WordBooksEntity wordBook = wordBooksRepository.findByWordBookId(wordBookId);
        if (wordBook == null) {
            throw new IllegalArgumentException("단어장을 찾을 수 없습니다.");
        }
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        if (newTitle.length() > 100) {
            throw new IllegalArgumentException("제목이 너무 깁니다. (최대 100자)");
        }
        wordBook.setWordBookTitle(newTitle.trim());
        wordBooksRepository.saveAndFlush(wordBook);
    }

    public List<WordBooksEntity> getWordBooks(String userId) {
        System.out.println("getWordBooks");
        return wordBooksRepository.findByUserId(userId);
    }

    @Transactional
    public String deleteWordbooks(Integer wordBookId, String userId) {
        System.out.println("deleteWordbooks");

        if (wordBooksRepository.existsById(wordBookId)) {
            wordBooksRepository.deleteByWordBookId(wordBookId);
            List<FilesEntity> filesEntityList = filesRepository.findByTableIdAndCategoryAndUserId(wordBookId, FilesEntity.Category.WORD, userId);
            System.out.println("filesEntityList: " + filesEntityList);
            System.out.println("filesEntityList size: " + filesEntityList.size());
            if (filesEntityList.size() > 0) {
                filesRepository.deleteByTableIdAndCategoryAndUserId(wordBookId, FilesEntity.Category.WORD, userId);
                return "true";
            }
            return "true";
        }
        return "false";
    }
}