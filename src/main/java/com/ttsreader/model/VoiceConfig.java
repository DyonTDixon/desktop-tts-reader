package com.ttsreader.model;

import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.VoiceId;

public record VoiceConfig(VoiceId voiceId, Engine engine) {

    // Default Neural voice setup (Matthew or Joanna sound very natural)
    public static VoiceConfig defaultNeural() {
        return new VoiceConfig(VoiceId.MATTHEW, Engine.NEURAL);
    }
}