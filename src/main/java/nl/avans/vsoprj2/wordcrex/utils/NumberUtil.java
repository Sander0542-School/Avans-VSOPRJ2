package nl.avans.vsoprj2.wordcrex.utils;

public class NumberUtil {
    public static Integer tryParse(String number) {
        return tryParse(number, null);
    }

    public static Integer tryParse(String number, Integer defaultValue) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
