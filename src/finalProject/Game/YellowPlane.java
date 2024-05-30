package finalProject.Game;

import javafx.scene.paint.Color;

public class YellowPlane extends Plane {
    public YellowPlane() {
        super(new Color[][] {
                { null, Color.YELLOW, Color.YELLOW },
                { Color.YELLOW, null, Color.YELLOW },
                { null, Color.YELLOW, null } }, Color.YELLOW);
    }
}