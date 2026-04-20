import java.awt.Color;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

import utils.Benchmark;
import utils.FileUtils;
import utils.Utils;

public class _03_Threadpool {
    private static final int NUM_THREADS = 5;

    public static void main(String[] args) throws InterruptedException {
        Benchmark.startMeasurement();

        Color[][] image = FileUtils.loadTestImage();
        int total_pixels = image.length * image[0].length;

        AtomicIntegerArray hist = new AtomicIntegerArray(256);
        int[] cumulative = new int[256];

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch latch1 = new CountDownLatch(NUM_THREADS);

        Benchmark.snapshotMemory();

        // 1. Compute luminosity histogram for this thread's part of the image
        for (int i = 0; i < NUM_THREADS; i++) {
            int threadIndex = i;

            Runnable task = () -> {
                int startY = threadIndex * image.length / NUM_THREADS;
                int endY = (threadIndex + 1) * image.length / NUM_THREADS;

                int localHist[] = new int[256];

                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < image[y].length; x++) {
                        Color pixel = image[y][x];
                        int lum = Utils.computeLuminosity(pixel);
                        localHist[lum]++;
                    }
                }

                Benchmark.snapshotMemory();

                for (int j = 0; j < 256; j++) {
                    hist.addAndGet(j, localHist[j]);
                }

                Benchmark.snapshotMemory();
                latch1.countDown();
            };

            executor.submit(task);
        }

        Benchmark.snapshotMemory();
        latch1.await();

        CountDownLatch latch2 = new CountDownLatch(NUM_THREADS);

        // 2. Compute cumulative luminosity histogram
        cumulative[0] = hist.get(0);
        for (int i = 1; i < 256; i++) {
            cumulative[i] = cumulative[i - 1] + hist.get(i);
        }

        // 3. Rewrite pixels with new luminosity values based on cumulative histogram
        for (int i = 0; i < NUM_THREADS; i++) {
            int threadIndex = i;

            Runnable task = () -> {
                int startY = threadIndex * image.length / NUM_THREADS;
                int endY = (threadIndex + 1) * image.length / NUM_THREADS;

                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < image[y].length; x++) {
                        Color pixel = image[y][x];
                        int lum = Utils.computeNewLuminosity(pixel, cumulative, total_pixels);
                        image[y][x] = new Color(lum, lum, lum);
                    }
                }

                latch2.countDown();
            };

            executor.submit(task);
        }

        latch2.await();
        Benchmark.snapshotMemory();
        executor.shutdownNow();
        Benchmark.snapshotMemory();

        FileUtils.writeTestImage(image);
        Benchmark.endMeasurement();
    }
}
