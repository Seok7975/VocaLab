package com.dev.vocalab.files;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilesDTO {
    private String userId;
    private String compileId;
    private MultipartFile file;
    private String fileType;
    private String fileExtension;
    private String saveDir;
    private String subDir;
    private String originalFileName;

    public FilesDTO(String userId, String compileId, String originalFileName) {
        this.userId = userId;
        this.compileId = compileId;
        this.originalFileName = originalFileName;
    }

    public FilesDTO(String saveDir, String originalFileName, String fileExtension, String subDir) {
        this.saveDir = saveDir;
        this.originalFileName = originalFileName;
        this.fileExtension = fileExtension;
        this.subDir = subDir;
    }

}