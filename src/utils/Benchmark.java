package utils;

import java.lang.management.*;

public class Benchmark {
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    private static long startTime;
    private static long startCpuTime;
    private static long maxMemory;

    public static void startMeasurement() {
        maxMemory = usedHeap();
        startCpuTime = totalCpuTime();
        startTime = System.nanoTime();
    }

    public static void snapshotMemory() {
        long currentMemory = usedHeap();

        if (currentMemory > maxMemory)
            maxMemory = currentMemory;
    }

    public static void endMeasurement() {
        long endTime = System.nanoTime();
        long endCpuTime = totalCpuTime();
        snapshotMemory();

        long wallTime = endTime - startTime;
        long cpuTime = endCpuTime - startCpuTime;

        System.out.printf("Wall time (ms): %.2f\n", wallTime / 1_000_000.0);
        System.out.printf("CPU time (ms): %.2f\n", cpuTime / 1_000_000.0);
        System.out.printf("Max memory used (MB): %.2f\n", maxMemory / (1024.0 * 1024.0));
    }

    private static long usedHeap() {
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        return heap.getUsed();
    }

    private static long totalCpuTime() {
        long total = 0;

        for (long id : threadBean.getAllThreadIds()) {
            long time = threadBean.getThreadCpuTime(id);

            if (time != -1)
                total += time;
        }

        return total;
    }
}