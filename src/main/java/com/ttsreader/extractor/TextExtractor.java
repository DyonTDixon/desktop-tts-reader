package com.ttsreader.extractor;

public interface TextExtractor<T> {
    String extractText(T source) throws Exception;
}
