import java.awt.Color;

import utils.Benchmark;
import utils.FileUtils;
import utils.Utils;

public class _01_Sequential {
    public static void main(String[] args) {
        Benchmark.startMeasurement();

        Color[][] image = FileUtils.loadTestImage();
        int total_pixels = image.length * image[0].length;

        int[] hist = new int[256];
        int[] cumulative = new int[256];

        Benchmark.snapshotMemory();

        // 1. Compute luminosity histogram
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[y].length; x++) {
                Color pixel = image[y][x];
                int lum = Utils.computeLuminosity(pixel);
                hist[lum]++;
            }
        }

        Benchmark.snapshotMemory();

        // 2. Compute cumulative luminosity histogram
        cumulative[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i - 1] + hist[i];
        }

        Benchmark.snapshotMemory();

        // 3. Rewrite pixels
        for (int y = 0; y < image.length; y++) {
            for (int x = 0; x < image[y].length; x++) {
                Color pixel = image[y][x];
                int lum = Utils.computeNewLuminosity(pixel, cumulative, total_pixels);
                image[y][x] = new Color(lum, lum, lum);
            }
        }

        Benchmark.snapshotMemory();

        FileUtils.writeTestImage(image);
        Benchmark.endMeasurement();
    }
}
