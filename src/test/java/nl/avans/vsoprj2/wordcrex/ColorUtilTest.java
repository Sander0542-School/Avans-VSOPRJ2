package nl.avans.vsoprj2.wordcrex;

import javafx.scene.paint.Color;
import junit.framework.TestCase;

public class ColorUtilTest extends TestCase {

    public void testRgbToHex() {
        String hex = Colors.toRGBCode(Color.rgb(84, 217, 152));

        assertEquals("#54D998", hex);
    }
}
