package com.dev.vocalab.board;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BoardFunction {
    // 전체 포맷 메서드
    public static String getFullFormattedDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd:HH:mm:ss");
        return dateTime.format(formatter);
    }

    // 간략 포맷 메서드
    public static String getShortFormattedDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd");
        return dateTime.format(formatter);
    }
}
