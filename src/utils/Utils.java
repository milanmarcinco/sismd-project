package utils;

import java.awt.Color;

public class Utils {
    public static int computeLuminosity(Color pixel) {
        int r = pixel.getRed();
        int g = pixel.getGreen();
        int b = pixel.getBlue();

        return computeLuminosity(r, g, b);
    }

    public static int computeNewLuminosity(Color pixel, int[] cumulative, int total_pixels) {
        int lum = Utils.computeLuminosity(pixel);
        int newLum = 255 * cumulative[lum] / total_pixels;
        return Math.clamp(newLum, 0, 255);
    }

    private static int computeLuminosity(int r, int g, int b) {
        return (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
    }
}
