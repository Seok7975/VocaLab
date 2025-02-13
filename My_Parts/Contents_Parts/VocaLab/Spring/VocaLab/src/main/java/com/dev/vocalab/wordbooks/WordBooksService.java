package com.dev.vocalab.wordbooks;

import com.dev.vocalab.files.FilesEntity;
import com.dev.vocalab.files.FilesRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordBooksService {

    private final FilesRepository filesRepository;
    private final WordBooksRepository wordBooksRepository;
    // Inject the static file path from application.properties
    @Value("${spring.web.resources.static-locations}")
    private String staticLocations;

    public List<String[]> readWordBook(Integer wordBookId) throws Exception {

        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD)
                .stream()
                .findFirst();
        System.out.println("fileEntityOpt: " +fileEntityOpt);

        if (fileEntityOpt.isEmpty()) {
            throw new Exception("No file found for the given wordBookId.");
        }

        // Extract the filePath from the retrieved FilesEntity
        String relativeFilePath = fileEntityOpt.get().getFilePath();
        System.out.println("Relative file path: " + relativeFilePath);

        // Ensure the file path is absolute
        String baseStaticPath = staticLocations.split(",")[1].replace("file:", "");
        String absoluteFilePath = Paths.get(baseStaticPath, relativeFilePath).toString();
        System.out.println("Absolute file path: " + absoluteFilePath);

        // Check if file exists
        if (!Files.exists(Paths.get(absoluteFilePath))) {
            throw new Exception("The file does not exist: " + absoluteFilePath);
        }

        // Read the CSV file
        List<String[]> wordList = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(absoluteFilePath))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                wordList.add(line);
            }
        } catch (Exception e) {
            throw new Exception("Failed to read the CSV file: " + absoluteFilePath, e);
        }
        return wordList;
    }
    public void deleteWord(Integer wordBookId, List<String> deleteWordList) throws Exception {
        Optional<FilesEntity> fileEntityOpt = filesRepository.findByTableIdAndCategory(wordBookId, FilesEntity.Category.WORD)
                .stream()
                .findFirst();
        System.out.println("fileEntityOpt: " + fileEntityOpt);

        if (fileEntityOpt.isEmpty()) {
            throw new Exception("No file found for the given wordBookId.");
        }

        // Extract the filePath from the retrieved FilesEntity
        String relativeFilePath = fileEntityOpt.get().getFilePath();
        System.out.println("Relative file path: " + relativeFilePath);

        // Ensure the file path is absolute
        String baseStaticPath = staticLocations.split(",")[1].replace("file:", "");
        String absoluteFilePath = Paths.get(baseStaticPath, relativeFilePath).toString();
        System.out.println("Absolute file path: " + absoluteFilePath);

        // Check if file exists
        if (!Files.exists(Paths.get(absoluteFilePath))) {
            throw new Exception("The file does not exist: " + absoluteFilePath);
        }

        // Read the CSV file and filter out words to delete
        List<String[]> remainingWords = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(absoluteFilePath))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                // Skip the line if the word (first column) is in the delete list
                if (line.length > 0 && !deleteWordList.contains(line[0])) {
                    remainingWords.add(line);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to read the CSV file: " + absoluteFilePath, e);
        }

        // Write the filtered content back to the file
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(absoluteFilePath))) {
            csvWriter.writeAll(remainingWords);
        } catch (IOException e) {
            throw new Exception("Failed to write to the CSV file: " + absoluteFilePath, e);
        }
    }
    public void changeStatusOfBookmark(Integer wordBookId){
        WordBooksEntity wordBooks = wordBooksRepository.findByWordBookId(wordBookId);
        System.out.println("Before set: " + wordBooks.getBookmark());
        if (wordBooks.getBookmark()==false){
            wordBooks.setBookmark(true);
        }else {
            wordBooks.setBookmark(false);
        }
        System.out.println("After set: " + wordBooks.getBookmark());
        wordBooksRepository.saveAndFlush(wordBooks);
    }

    public WordBooksEntity loadWordBookStatus(Integer wordBookId){
        return wordBooksRepository.findByWordBookId(wordBookId);
    }

    public void updateWordBookTitle(Integer wordBookId, String newTitle) {
        WordBooksEntity wordBook = wordBooksRepository.findByWordBookId(wordBookId);
        if (wordBook == null) {
            throw new IllegalArgumentException("단어장을 찾을 수 없습니다.");
        }
        // 제목이 비어있는지 확인
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
        }
        // 제목 길이 제한 (예: 100자)
        if (newTitle.length() > 100) {
            throw new IllegalArgumentException("제목이 너무 깁니다. (최대 100자)");
        }
        wordBook.setWordBookTitle(newTitle.trim());
        wordBooksRepository.saveAndFlush(wordBook);
    }
}
