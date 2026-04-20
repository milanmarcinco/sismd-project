import java.awt.Color;

import utils.Benchmark;
import utils.FileUtils;
import utils.Utils;

public class _02_Multithreaded {
    private static final int NUM_THREADS = 5;

    public static void main(String[] args) {
        Benchmark.startMeasurement();

        Color[][] image = FileUtils.loadTestImage();
        int total_pixels = image.length * image[0].length;

        int[] hist = new int[256];
        int[] cumulative = new int[256];

        Thread[] threads = new Thread[NUM_THREADS];

        Benchmark.snapshotMemory();

        for (int i = 0; i < NUM_THREADS; i++) {
            int threadIndex = i;

            Thread t = new Thread(() -> {
                int startY = threadIndex * image.length / NUM_THREADS;
                int endY = (threadIndex + 1) * image.length / NUM_THREADS;

                int localHist[] = new int[256];

                // 1. Compute luminosity histogram for this thread's part of the image
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < image[y].length; x++) {
                        Color pixel = image[y][x];
                        int lum = Utils.computeLuminosity(pixel);
                        localHist[lum]++;
                    }
                }

                synchronized (hist) {
                    for (int j = 0; j < 256; j++) {
                        hist[j] += localHist[j];
                    }
                }
            });

            threads[i] = t;
            t.start();
        }

        Benchmark.snapshotMemory();

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Benchmark.snapshotMemory();

        // 2. Compute cumulative luminosity histogram
        cumulative[0] = hist[0];
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i - 1] + hist[i];
        }

        Benchmark.snapshotMemory();

        for (int i = 0; i < NUM_THREADS; i++) {
            int threadIndex = i;

            Thread t = new Thread(() -> {
                int startY = threadIndex * image.length / NUM_THREADS;
                int endY = (threadIndex + 1) * image.length / NUM_THREADS;

                // 3. Rewrite pixels
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < image[y].length; x++) {
                        Color pixel = image[y][x];
                        int lum = Utils.computeNewLuminosity(pixel, cumulative, total_pixels);
                        image[y][x] = new Color(lum, lum, lum);
                    }
                }
            });

            threads[i] = t;
            t.start();
        }

        Benchmark.snapshotMemory();

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Benchmark.snapshotMemory();

        FileUtils.writeTestImage(image);
        Benchmark.endMeasurement();
    }
}
