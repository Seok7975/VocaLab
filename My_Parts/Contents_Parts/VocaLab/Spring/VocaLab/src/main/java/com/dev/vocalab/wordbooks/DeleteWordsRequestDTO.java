package com.dev.vocalab.wordbooks;

import java.util.List;

public class DeleteWordsRequestDTO {
    private Integer wordBookId;
    private List<String> words;

    // Getters and Setters
    public Integer getWordBookId() {
        return wordBookId;
    }

    public void setWordBookId(Integer wordBookId) {
        this.wordBookId = wordBookId;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }
}
