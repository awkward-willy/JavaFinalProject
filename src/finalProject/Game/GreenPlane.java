package finalProject.Game;

import javafx.scene.paint.Color;

public class GreenPlane extends Plane {
    public GreenPlane() {
        super(new Color[][] {
                { null, Color.GREEN, null },
                { Color.GREEN, Color.GREEN, Color.GREEN },
                { null, Color.GREEN, null }
        }, Color.GREEN, "../picture/GreenPlane.png");
    }
}