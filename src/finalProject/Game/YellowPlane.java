package finalProject.Game;

import javafx.scene.paint.Color;

public class YellowPlane extends Plane {
    public YellowPlane() {
        super(new Color[][] {
                { null, null, null, null, null },
                { null, null, Color.YELLOW, null, null },
                { null, null, Color.YELLOW, null, null },
                { null, Color.YELLOW, Color.YELLOW, Color.YELLOW, null },
                { null, Color.YELLOW, null, Color.YELLOW, null },
        }, Color.YELLOW, "../picture/YellowPlane.png");
    }
}
