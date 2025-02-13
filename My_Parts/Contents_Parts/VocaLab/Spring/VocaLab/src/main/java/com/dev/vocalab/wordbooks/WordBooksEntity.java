package com.dev.vocalab.wordbooks;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "WordBooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordBooksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wordBookId;

    @Column(nullable = false, length = 255)
    private String wordBookTitle;

    @Column(nullable = false)
    private Boolean bookmark;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String userId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.bookmark == null) {
            this.bookmark = false; // Default value for bookmark
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
