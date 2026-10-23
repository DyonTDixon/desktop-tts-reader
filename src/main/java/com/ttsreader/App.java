package com.ttsreader;

import com.ttsreader.extractor.PdfExtractor;
import com.ttsreader.extractor.WebArticleExtractor;
import com.ttsreader.model.VoiceConfig;
import com.ttsreader.service.AudioPlayerService;
import com.ttsreader.service.PollyTtsService;
import com.ttsreader.service.TextCleanerService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.File;
import java.io.InputStream;

public class App extends Application {

    private final PdfExtractor pdfExtractor = new PdfExtractor();
    private final WebArticleExtractor webExtractor = new WebArticleExtractor();
    private final TextCleanerService textCleaner = new TextCleanerService();
    private final PollyTtsService pollyService = new PollyTtsService();
    private final AudioPlayerService audioPlayer = new AudioPlayerService();

    @Override
    public void start(Stage stage) {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Paste text here, or use the buttons above to extract text from a PDF, Webpage, or Screenshot...");
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        TextField urlField = new TextField();
        urlField.setPromptText("https://example.com/article");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        ComboBox<VoiceId> voiceBox = new ComboBox<>();
        voiceBox.getItems().addAll(VoiceId.MATTHEW, VoiceId.JOANNA, VoiceId.RUTH, VoiceId.STEPHEN);
        voiceBox.setValue(VoiceId.MATTHEW);

        Button btnPdf = new Button("📄 Load PDF");
        Button btnUrl = new Button("🌐 Fetch URL");
        Button btnSnip = new Button("✂️ Snip Screen");
        Button btnSpeak = new Button("🔊 Speak");
        Button btnStop = new Button("⏹ Stop");

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

        // 3. Speak Button Action (AWS Polly + JLayer)
        btnSpeak.setOnAction(e -> {
            String textToRead = textArea.getText().trim();
            if (textToRead.isEmpty()) {
                statusLabel.setText("Nothing to read! Enter or extract some text first.");
                return;
            }

            VoiceConfig config = new VoiceConfig(voiceBox.getValue(), Engine.NEURAL);
            statusLabel.setText("Requesting audio from AWS Polly (" + config.voiceId() + ")...");

            new Thread(() -> {
                try {
                    InputStream audioStream = pollyService.synthesize(textToRead, config);
                    Platform.runLater(() -> statusLabel.setText("Playing audio..."));

                    audioPlayer.playAsync(
                            audioStream,
                            () -> Platform.runLater(() -> statusLabel.setText("Playback finished.")),
                            err -> Platform.runLater(() -> statusLabel.setText("Playback error: " + err.getMessage()))
                    );
                } catch (Exception ex) {
                    Platform.runLater(() -> statusLabel.setText("AWS Polly Error: " + ex.getMessage()));
                }
            }).start();
        });

        // 4. Stop Button Action
        btnStop.setOnAction(e -> {
            audioPlayer.stop();
            statusLabel.setText("Playback stopped.");
        });

        HBox topBar = new HBox(8, btnPdf, urlField, btnUrl, btnSnip, voiceBox, btnSpeak, btnStop);
        VBox root = new VBox(12, topBar, textArea, statusLabel);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 850, 520);
        stage.setTitle("Desktop AI Text-to-Speech Reader");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        audioPlayer.stop();
        pollyService.close();
    }

    public static void main(String[] args) {
        launch();
    }
}