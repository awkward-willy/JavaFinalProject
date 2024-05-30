package finalProject.Game;

import javafx.scene.paint.Color;

public abstract class Plane {
    protected Color[][] shape;
    protected Color color;
    private boolean destroyed = false;

    public Plane(Color[][] shape, Color color) {
        this.shape = shape;
        this.color = color;
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
}