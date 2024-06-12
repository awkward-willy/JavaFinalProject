package finalProject.Game;

import javafx.scene.paint.Color;

public class BluePlane extends Plane {
    public BluePlane() {
        super(new Color[][] {
                { null, null, Color.BLUE, null, null },
                { null, null, Color.BLUE, null, null },
                { Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE },
                { null, null, Color.BLUE, null, null },
                { null, Color.BLUE, Color.BLUE, Color.BLUE, null }
        }, Color.BLUE, "../picture/BluePlane.png");
    }
}