import java.awt.Color;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

import utils.Benchmark;
import utils.FileUtils;
import utils.Utils;

public class _04_ForkJoin {
    public static void main(String[] args) {
        Benchmark.startMeasurement();

        Color[][] image = FileUtils.loadTestImage();
        int total_pixels = image.length * image[0].length;

        ForkJoinPool pool = new ForkJoinPool();

        Benchmark.snapshotMemory();

        HistogramTask histogramTask = new HistogramTask(image, 0, image.length);
        int[] hist = pool.invoke(histogramTask);

        Benchmark.snapshotMemory();

        int[] cumulative = new int[256];
        cumulative[0] = hist[0];
        for (int i = 1; i < 256; i++)
            cumulative[i] = cumulative[i - 1] + hist[i];

        UpdateTask updateTask = new UpdateTask(image, cumulative, 0, image.length, total_pixels);
        pool.invoke(updateTask);

        Benchmark.snapshotMemory();

        pool.close();

        Benchmark.snapshotMemory();

        FileUtils.writeTestImage(image);
        Benchmark.endMeasurement();
    }
}

class HistogramTask extends RecursiveTask<int[]> {
    private static final int ROWS_THRESHOLD = 200;

    private Color[][] image;
    private int startY, endY;

    public HistogramTask(Color[][] image, int startY, int endY) {
        this.image = image;
        this.startY = startY;
        this.endY = endY;
    }

    @Override
    protected int[] compute() {
        if (endY - startY <= ROWS_THRESHOLD) {
            // Compute histogram for this part of the image

            int[] localHist = new int[256];

            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < image[y].length; x++) {
                    Color pixel = image[y][x];
                    int lum = Utils.computeLuminosity(pixel);
                    localHist[lum]++;
                }
            }

            Benchmark.snapshotMemory();
            return localHist;
        }

        // Otherwise, split the task into two subtasks
        int mid = (startY + endY) / 2;
        HistogramTask left = new HistogramTask(image, startY, mid);
        HistogramTask right = new HistogramTask(image, mid, endY);

        left.fork();
        right.fork();

        int[] rightHist = right.join();
        int[] leftHist = left.join();

        int[] merged = new int[256];
        for (int i = 0; i < 256; i++)
            merged[i] = leftHist[i] + rightHist[i];

        Benchmark.snapshotMemory();

        return merged;
    }
}

class UpdateTask extends RecursiveAction {
    private static final int ROWS_THRESHOLD = 200;

    private Color[][] image;
    private int[] cumulative;
    private int startY, endY;
    private int total_pixels;

    public UpdateTask(Color[][] image, int[] cumulative, int startY, int endY, int total_pixels) {
        this.image = image;
        this.cumulative = cumulative;
        this.startY = startY;
        this.endY = endY;
        this.total_pixels = total_pixels;
    }

    @Override
    protected void compute() {
        if (endY - startY <= ROWS_THRESHOLD) {
            // Compute and set new luminosity for each pixel in this part of the image

            for (int y = startY; y < endY; y++) {
                for (int x = 0; x < image[y].length; x++) {
                    Color pixel = image[y][x];
                    int lum = Utils.computeNewLuminosity(pixel, cumulative, total_pixels);
                    image[y][x] = new Color(lum, lum, lum);
                }
            }

            Benchmark.snapshotMemory();
            return;
        }

        // Otherwise split the task into two subtasks
        int mid = (startY + endY) / 2;
        UpdateTask left = new UpdateTask(image, cumulative, startY, mid, total_pixels);
        UpdateTask right = new UpdateTask(image, cumulative, mid, endY, total_pixels);

        left.fork();
        right.fork();

        right.join();
        left.join();

        Benchmark.snapshotMemory();
    }
}
