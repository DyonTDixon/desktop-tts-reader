package com.ttsreader.ui;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class ScreenSnipperOverlay {

    private double startX;
    private double startY;

    public void show(Stage mainStage, Consumer<java.awt.Rectangle> onAreaSelected) {
        // Minimize the main app window so it doesn't block what you want to snip
        mainStage.setIconified(true);

        Stage overlayStage = new Stage(StageStyle.TRANSPARENT);
        overlayStage.setAlwaysOnTop(true);

        Pane root = new Pane();
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25);");
        root.setCursor(Cursor.CROSSHAIR);

        Rectangle selectionBox = new Rectangle();
        selectionBox.setFill(Color.rgb(0, 120, 215, 0.2));
        selectionBox.setStroke(Color.rgb(0, 120, 215));
        selectionBox.setStrokeWidth(2);
        root.getChildren().add(selectionBox);

        root.setOnMousePressed(e -> {
            startX = e.getScreenX();
            startY = e.getScreenY();
            selectionBox.setX(startX);
            selectionBox.setY(startY);
            selectionBox.setWidth(0);
            selectionBox.setHeight(0);
        });

        root.setOnMouseDragged(e -> {
            double currentX = e.getScreenX();
            double currentY = e.getScreenY();
            selectionBox.setX(Math.min(startX, currentX));
            selectionBox.setY(Math.min(startY, currentY));
            selectionBox.setWidth(Math.abs(currentX - startX));
            selectionBox.setHeight(Math.abs(currentY - startY));
        });

        root.setOnMouseReleased(e -> {
            int x = (int) selectionBox.getX();
            int y = (int) selectionBox.getY();
            int width = (int) selectionBox.getWidth();
            int height = (int) selectionBox.getHeight();

            overlayStage.close();

            if (width > 10 && height > 10) {
                // Brief pause so the blue overlay disappears before Robot captures the screen
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                        onAreaSelected.accept(new java.awt.Rectangle(x, y, width, height));
                    } catch (InterruptedException ignored) {
                    }
                }).start();
            } else {
                mainStage.setIconified(false);
            }
        });

        var screenBounds = Screen.getPrimary().getBounds();
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        scene.setFill(Color.TRANSPARENT);

        // Press ESC to cancel snipping
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                overlayStage.close();
                mainStage.setIconified(false);
            }
        });

        overlayStage.setScene(scene);
        overlayStage.show();
    }
}