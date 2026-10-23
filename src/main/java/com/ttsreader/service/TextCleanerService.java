package com.ttsreader.service;

public class TextCleanerService {

    public String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        return rawText
                // Fix hyphenated words split across PDF lines (e.g., "devel-\nopment" -> "development")
                .replaceAll("-\\r?\\n", "")
                // Replace single line breaks inside paragraphs with a space
                .replaceAll("(?<!\\n)\\r?\\n(?!\\r?\\n)", " ")
                // Collapse multiple spaces into a single space
                .replaceAll("[ \\t]+", " ")
                .trim();
    }
}