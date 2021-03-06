package nl.avans.vsoprj2.wordcrex;

import javafx.scene.paint.Color;

public class Colors {
    public static final Color PRIMARY = Color.rgb(32, 27, 83);
    public static final Color PRIMARY_DARK = Color.rgb(27, 23, 68);

    public static final Color ACCENT = Color.rgb(255, 187, 0);

    public static final Color ICON = Color.rgb(188, 186, 203);
    public static final Color TEXT = Color.rgb(32, 27, 83);

    public static String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
