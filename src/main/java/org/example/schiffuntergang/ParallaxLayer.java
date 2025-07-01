package org.example.schiffuntergang;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class ParallaxLayer {
    private final ImageView image1;
    private final ImageView image2;
    private final double speed;
    private final Pane layer = new Pane();
    private final Stage stage;

    public ParallaxLayer(String imagePath, double speed, Stage stage) {
        this.speed = speed;
        this.stage = stage;

        Image image = new Image(getClass().getResourceAsStream(imagePath));
        image1 = new ImageView(image);
        image2 = new ImageView(image);

        image1.setPreserveRatio(false);
        image2.setPreserveRatio(false);

        image1.fitHeightProperty().bind(stage.heightProperty());
        image2.fitHeightProperty().bind(stage.heightProperty());

        image1.fitWidthProperty().bind(stage.widthProperty());
        image2.fitWidthProperty().bind(stage.widthProperty());

        image1.setTranslateX(0);
        image2.setTranslateX(image1.getBoundsInParent().getWidth());

        layer.getChildren().addAll(image1, image2);

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            image2.setTranslateX(image1.getTranslateX() + stage.getWidth());
        });
    }

    public Pane getNode() {
        return layer;
    }

    public void update() {
        double dx = -speed;

        image1.setTranslateX(image1.getTranslateX() + dx);
        image2.setTranslateX(image2.getTranslateX() + dx);

        if (image1.getTranslateX() + stage.getWidth() <= 0) {
            image1.setTranslateX(image2.getTranslateX() + stage.getWidth());
        }
        if (image2.getTranslateX() + stage.getWidth() <= 0) {
            image2.setTranslateX(image1.getTranslateX() + stage.getWidth());
        }
    }
}
