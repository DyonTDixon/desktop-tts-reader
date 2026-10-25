package com.ttsreader.service;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

public class AudioPlayerService {

    private volatile Player currentPlayer;
    private volatile InputStream currentStream;
    private volatile Thread playbackThread;
    private volatile boolean manuallyStopped = false;

    public synchronized void playAsync(InputStream mp3Stream, Runnable onComplete, Consumer<Exception> onError) {
        stop(); // Ensure any running audio or request is stopped first
        manuallyStopped = false;
        currentStream = new BufferedInputStream(mp3Stream);

        playbackThread = new Thread(() -> {
            try (InputStream stream = currentStream) {
                currentPlayer = new Player(stream);
                currentPlayer.play(); // Plays until EOF or stop() is called

                if (!manuallyStopped && onComplete != null) {
                    onComplete.run();
                }
            } catch (Exception e) {
                if (!manuallyStopped && onError != null) {
                    onError.accept(e);
                }
            } finally {
                if (currentPlayer != null) {
                    currentPlayer.close();
                    currentPlayer = null;
                }
            }
        }, "TTS-Playback-Thread");

        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    public synchronized void stop() {
        manuallyStopped = true;

        if (currentPlayer != null) {
            currentPlayer.close();
            currentPlayer = null;
        }
        if (currentStream != null) {
            try {
                currentStream.close();
            } catch (Exception ignored) {
            }
            currentStream = null;
        }
        if (playbackThread != null && playbackThread.isAlive()) {
            playbackThread.interrupt();
            playbackThread = null;
        }
    }
}