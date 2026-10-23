package com.ttsreader;

import com.ttsreader.extractor.PdfExtractor;
import com.ttsreader.extractor.WebArticleExtractor;
import com.ttsreader.service.TextCleanerService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class App extends Application {

    private final PdfExtractor pdfExtractor = new PdfExtractor();
    private final WebArticleExtractor webExtractor = new WebArticleExtractor();
    private final TextCleanerService textCleaner = new TextCleanerService();

    @Override
    public void start(Stage stage) {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Paste text here, or use the buttons above to extract text from a PDF, Webpage, or Screenshot...");
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        TextField urlField = new TextField();
        urlField.setPromptText("https://example.com/article");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        Button btnPdf = new Button("📄 Load PDF");
        Button btnUrl = new Button("🌐 Fetch URL");
        Button btnSnip = new Button("✂️ Snip Screen (OCR)");
        Button btnSpeak = new Button("🔊 Speak (Polly)");

        Label statusLabel = new Label("Ready.");

        // 1. PDF Button Action
        btnPdf.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open PDF File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                try {
                    statusLabel.setText("Extracting PDF: " + selectedFile.getName() + "...");
                    String raw = pdfExtractor.extractText(selectedFile);
                    textArea.setText(textCleaner.clean(raw));
                    statusLabel.setText("Loaded PDF successfully.");
                } catch (Exception ex) {
                    statusLabel.setText("Error reading PDF: " + ex.getMessage());
                }
            }
        });

        // 2. Web URL Button Action
        btnUrl.setOnAction(e -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                statusLabel.setText("Please enter a valid URL first.");
                return;
            }
            try {
                statusLabel.setText("Fetching article...");
                String raw = webExtractor.extractText(url);
                textArea.setText(textCleaner.clean(raw));
                statusLabel.setText("Fetched web article successfully.");
            } catch (Exception ex) {
                statusLabel.setText("Error fetching URL: " + ex.getMessage());
            }
        });

        HBox topBar = new HBox(10, btnPdf, urlField, btnUrl, btnSnip, btnSpeak);
        VBox root = new VBox(12, topBar, textArea, statusLabel);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 750, 500);
        stage.setTitle("Desktop AI Text-to-Speech Reader");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}