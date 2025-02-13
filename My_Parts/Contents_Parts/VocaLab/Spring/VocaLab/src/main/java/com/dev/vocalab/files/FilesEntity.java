package com.dev.vocalab.files;

import com.dev.vocalab.users.UsersEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Integer fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "filePath", nullable = false, length = 255)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "fileType", nullable = false)
    private FileType fileType;

    @Column(name = "uploadedAt", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "tableId")
    private Integer tableId;

    @Column(name = "userId", nullable = false, length = 100)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", insertable = false, updatable = false)
    private UsersEntity user;

    public enum Category {
        BOARD, COMPILE, ESSAY, NONE, PROFILE, TEST, TESTRECORD, WORD, COMPILERESULT
    }

    public enum FileType {
        FILE, IMAGE
    }
}