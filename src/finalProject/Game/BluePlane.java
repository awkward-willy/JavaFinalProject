package finalProject.Game;

import javafx.scene.paint.Color;

public class BluePlane extends Plane {
    public BluePlane() {
        super(new Color[][] {
                { null, Color.BLUE, Color.BLUE },
                { Color.BLUE, null, Color.BLUE },
                { null, Color.BLUE, null } }, Color.BLUE);
    }
}