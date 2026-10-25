package com.ttsreader.extractor;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

public class OcrScreenshotExtractor implements TextExtractor<Rectangle> {

    private final ITesseract tesseract;

    public OcrScreenshotExtractor() {
        this.tesseract = new Tesseract();
        // Points to desktop-tts-reader/tessdata/eng.traineddata
        this.tesseract.setDatapath(new File("tessdata").getAbsolutePath());
        this.tesseract.setLanguage("eng");
    }

    @Override
    public String extractText(Rectangle screenArea) throws Exception {
        Robot robot = new Robot();
        BufferedImage capture = robot.createScreenCapture(screenArea);
        return tesseract.doOCR(capture);
    }
}