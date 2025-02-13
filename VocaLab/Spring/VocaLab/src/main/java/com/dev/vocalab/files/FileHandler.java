package com.dev.vocalab.files;


import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FileHandler {

    private static final RestTemplate restTemplate = new RestTemplate();

    // [ DB에 저장할 filePath 생성 메서드 ]
    public static String renameFile(String saveDir, String fileName) {
        System.out.println("FileHandler - renameFile");
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        uuid = uuid.replaceAll("-", "");
        // 새로운 파일명
        String newFileName = saveDir + File.separator + uuid + "_" + fileName;
        // 기존파일, 새로운 파일의 객체생성 후 이름 변경
//        File file = new File(saveDir + File.separator + fileName);
//        File newFile = new File(saveDir + File.separator + newFileName);
//        file.renameTo(newFile);
        // 변경된 파일명을 반환
        return newFileName;
    }

    // [ 파일 디렉토리 지정 메서드]
    public static FilesDTO spesifyFilePath(FilesDTO filesDto) {
        System.out.println("FileHandler - spesifyFilePath");
        // 1. 물리적 경로 얻기
        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/compileRecord/";
        // 2. 파일을 저장할 경로(실제 서버파일 시스템의 절대경로로 변환)
        // 2-1. 업로드 일자, 사용자Id, compileId(compileRecord테이블) 변수에 저장
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String userId = filesDto.getUserId();
        String compileId = filesDto.getCompileId();
        // 2-3. 저장디렉토리 경로 변수 정장 (yyyy/mm/dd/userId/compileId)
        String subDir = LocalDate.now().format(date) + File.separator + userId + File.separator + compileId;

        // 3. 파일명, 확장자 추출
        // 3-1. filesDto에서 originalFileName이 비어있으면 multipartFile에서 파일이름 추출.
        String originalFileName;
        System.out.println("spesifyFilePath - originalFileName-:");
        if (filesDto.getOriginalFileName().contains("csv")) {// 값이 있으면 compileResult(추출 결과)의 csv파일의 이름이다. (compileResult_compileId.csv)
            originalFileName = filesDto.getOriginalFileName();
            System.out.println("compileResult csv - originfilename-" + originalFileName + "-");
        } else {
            originalFileName = filesDto.getFile().getOriginalFilename();
            System.out.println("mutipartFile - originfilename-" + originalFileName + "-");
        }
        // 3-3. 파일의 확장자 추출
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        // 3-4. 파일 확장자에 따라 파일명이 달라짐
        System.out.println("FileExtension : " + fileExtension);
        if (fileExtension.trim().equals("jpg") || fileExtension.trim().equals("jpeg") || fileExtension.trim().equals("png")) {
            //이미지 파일의 저장 경로(yyyy/mm/dd/userId/compileId/img)
            subDir += File.separator + "img";
            filesDto.setFileType("IMAGE");
            System.out.println(filesDto.getFileType());
        } else {
            //기타문서 파일의 저장 경로(yyyy/mm/dd/userId/compileId/doc)
            subDir += File.separator + "doc";
            filesDto.setFileType("FILE");
            System.out.println(filesDto.getFileType());
        }// if
        String saveDir = uploadDir + subDir;
        System.out.println("save dir : " + saveDir);

        // 최종 파일 경로
        System.out.println("final_saveDir" + saveDir);
        filesDto.setSubDir(subDir);
        filesDto.setSaveDir(saveDir);
        filesDto.setOriginalFileName(originalFileName);
        filesDto.setFileExtension(fileExtension);
        return filesDto;
    }

    // [ 파일 디렉토리 생성 메서드 ]
    public static void createDirectory(String saveDir) {
        System.out.println("FileHandler - createDirectory");
        try {
            Path path = Paths.get(saveDir); // 전달받은 경로를 Path 객체로 변환
            if (!Files.exists(path)) { // 경로가 존재하지 않는 경우
                Files.createDirectories(path); // 디렉터리 생성
                System.out.println("디렉터리가 성공적으로 생성되었습니다: " + saveDir);
            } else {
                System.out.println("디렉터리가 이미 존재합니다: " + saveDir);
            }
        } catch (IOException e) {
            // 예외 처리
            System.err.println("디렉터리를 생성하는 동안 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // [ home에서 단어추출 btn 클릭 후 Python으로 API 전송 메서드 리턴값(json type)을 compilePro에서 사용함] -미완
    // [ Python api 전송 ]
    public static List<Map<String, Object>> AICompileWords(String compileSource, List<FilesDTO> filesDto) {
        System.out.println("FileHandler - AICompileWords");
        System.out.println("List filesDto : " + filesDto);

        List<Map<String, Object>> fileDataList = new ArrayList<>(); // 파일 데이터 리스트

        try {
            // 파일이 있는지 확인
            if (filesDto != null && !filesDto.isEmpty()) {
                for (FilesDTO fileDto : filesDto) {
                    if (fileDto.getOriginalFileName() == null || fileDto.getOriginalFileName().isEmpty()) {
                        continue; // 또는 예외 처리
                    }

                    // 경로 정규화
                    Path normalizedPath = Paths.get(fileDto.getSaveDir()).normalize();

                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("filePath", normalizedPath.toString());
                    fileData.put("fileName", fileDto.getOriginalFileName());
                    fileData.put("fileType", fileDto.getFileExtension());
                    // 필요하다면 실제 파일 데이터도 포함
                    fileDataList.add(fileData);
                }
            }

            System.out.println("fileDataList : " + fileDataList);
            // Python 서버로 전송할 데이터 구성
            String pythonApiUrl = "http://im.21v.in:28119/apiCompile";
            Map<String, Object> params = new HashMap<>();
            params.put("compileSource", compileSource); // URL 데이터
            params.put("originalFiles", fileDataList); // 파일 리스트 데이터

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON); // JSON 형식으로 전송

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);

            // Python 서버로 요청 전송
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
            System.out.println("ai응답 : " + response.getBody());
            // 응답 처리
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("여기1");
                Map<String, Object> responseBody = response.getBody();
                Map<String, Object> responseMap = (Map<String, Object>) responseBody.get("response");
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                // JSON 텍스트 추출 및 전처리
                String jsonText = (String) parts.get(0).get("text");
                jsonText = jsonText.replace("```json", "").replace("```", "").trim();
                System.out.println("추출된 JSON 텍스트: " + jsonText);

                // JSON 텍스트를 JSONArray로 변환
                JSONArray jsonArray = new JSONArray(jsonText);

                // JSONArray를 List<Map<String, Object>>로 변환
                List<Map<String, Object>> result = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // JSONObject를 Map으로 변환
                    Map<String, Object> map = new HashMap<>();
                    for (String key : jsonObject.keySet()) {
                        map.put(key, jsonObject.get(key));
                    }
                    result.add(map);
                }

                return result;
            } else {
                System.out.println("여기2");
                throw new RuntimeException("Python 서버 호출 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("여기3");
            System.out.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Python 서버와 통신 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public static List<Map<String, Object>> saveResultCSV(List<Map<String, Object>> compileWordsJson, String saveCsv) {
        System.out.println("FileHandler - saveResultCSV");
        List<Map<String, Object>> wordList = new ArrayList<>();

        try (FileOutputStream fos = new FileOutputStream(saveCsv)) {
            // UTF-8 BOM 추가
            fos.write(0xEF);
            fos.write(0xBB);
            fos.write(0xBF);

            try (OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                // 헤더 작성
                writer.write("\"단어\",\"뜻\"\n");

                for (Map<String, Object> wordItem : compileWordsJson) {
                    if (wordItem == null || !wordItem.containsKey("단어") || !wordItem.containsKey("뜻")) {
                        throw new RuntimeException("단어 또는 뜻 데이터가 누락되었습니다: " + wordItem);
                    }

                    String word = (String) wordItem.get("단어");
                    String meaning = (String) wordItem.get("뜻");

                    // 품사 제거 (n., v., adj. 등을 제거)
                    meaning = meaning.replaceAll("^[a-z]+\\.", "").trim();

                    Map<String, Object> map = new HashMap<>();
                    map.put("word", word);
                    map.put("meaning", meaning);
                    wordList.add(map);

                    word = "\"" + word + "\"";
                    meaning = "\"" + meaning + "\"";

                    // CSV 행 작성
                    writer.write(word + "," + meaning + "\n");
                }
            }

            System.out.println("CSV 파일 저장 완료: " + saveCsv);
            return wordList;
        } catch (IOException e) {
            throw new RuntimeException("CSV 저장 중 오류 발생: " + e.getMessage(), e);
        }
    }


} // class
