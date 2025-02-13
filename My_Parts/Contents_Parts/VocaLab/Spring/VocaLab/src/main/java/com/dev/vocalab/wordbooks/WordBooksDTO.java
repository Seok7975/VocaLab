package com.dev.vocalab.wordbooks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordBooksDTO {
    private Long wordBookId;
    private String wordBookTitle;
    private Boolean bookmark;
    private String userId;
}
