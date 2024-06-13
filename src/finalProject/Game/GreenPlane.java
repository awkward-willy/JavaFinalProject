package finalProject.Game;

import javafx.scene.paint.Color;

public class GreenPlane extends Plane {
    public GreenPlane() {
        super(new Color[][] {
                { null, null, Color.GREEN, null, null },
                { null, null, Color.GREEN, null, null },
                { Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN },
                { null, Color.GREEN, Color.GREEN, Color.GREEN, null },
                { null, null, null, null, null },
        }, Color.GREEN, "../picture/GreenPlane.png");
    }
}