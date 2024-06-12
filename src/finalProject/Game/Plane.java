package finalProject.Game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public abstract class Plane {
    protected Color[][] shape;
    protected Color color;
    private boolean destroyed = false;
    private Image image;
    private String imagePath;
    private ImageView imageView;
    private int rotationAngle = 0;

    public Plane(Color[][] shape, Color color, String imagePath) {
        this.shape = shape;
        this.color = color;
        this.imagePath = imagePath;
        this.image = new Image(getClass().getResourceAsStream(imagePath));
        this.imageView = new ImageView(this.image);
    }

    public void rotate() {
        int size = shape.length;
        Color[][] newShape = new Color[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newShape[j][size - 1 - i] = shape[i][j];
            }
        }
        shape = newShape;
        rotationAngle = (rotationAngle + 90) % 360;
        imageView.setRotate(rotationAngle);
    }

    public Color[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public Image getImage() {
        return image;
    }

    public String getImagePath() {
        return imagePath;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
