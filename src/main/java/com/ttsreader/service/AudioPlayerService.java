package com.ttsreader.service;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class AudioPlayerService {

    private Player currentPlayer;
    private Thread playbackThread;

    public void playAsync(InputStream mp3Stream, Runnable onComplete, java.util.function.Consumer<Exception> onError) {
        stop(); // Stop any existing audio before starting a new one

        playbackThread = new Thread(() -> {
            try (BufferedInputStream buffered = new BufferedInputStream(mp3Stream)) {
                currentPlayer = new Player(buffered);
                currentPlayer.play();
                if (onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                if (onError != null) {
                    onError.accept(e);
                }
            }
        });

        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public void stop() {
        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
        }
    }
}