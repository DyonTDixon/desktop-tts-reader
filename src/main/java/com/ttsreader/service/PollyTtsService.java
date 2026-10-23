package com.ttsreader.service;

import com.ttsreader.model.VoiceConfig;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PollyTtsService implements AutoCloseable {

    private static final int MAX_CHARS = 2800; // Safe buffer under Polly's 3,000 char limit
    private final PollyClient pollyClient;

    public PollyTtsService() {
        // Automatically loads credentials from ~/.aws/credentials or environment variables
        this.pollyClient = PollyClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    public InputStream synthesize(String text, VoiceConfig config) {
        List<String> chunks = splitIntoChunks(text, MAX_CHARS);
        List<InputStream> audioStreams = new ArrayList<>();

        for (String chunk : chunks) {
            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(chunk)
                    .voiceId(config.voiceId())
                    .engine(config.engine())
                    .outputFormat(OutputFormat.MP3)
                    .build();

            audioStreams.add(pollyClient.synthesizeSpeech(request));
        }

        // Stitch all MP3 streams together into one continuous stream
        return new SequenceInputStream(Collections.enumeration(audioStreams));
    }

    private List<String> splitIntoChunks(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String remaining = text.trim();

        while (remaining.length() > maxLength) {
            // Try to split cleanly at the last period before the limit
            int splitIndex = remaining.lastIndexOf(". ", maxLength);
            if (splitIndex == -1) {
                splitIndex = remaining.lastIndexOf(" ", maxLength);
            }
            if (splitIndex == -1) {
                splitIndex = maxLength;
            } else {
                splitIndex += 1; // Include the period
            }

            chunks.add(remaining.substring(0, splitIndex).trim());
            remaining = remaining.substring(splitIndex).trim();
        }

        if (!remaining.isEmpty()) {
            chunks.add(remaining);
        }
        return chunks;
    }

    @Override
    public void close() {
        pollyClient.close();
    }
}