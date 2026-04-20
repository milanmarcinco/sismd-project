## To do

- make memory snapshotting thread-safe

## cdfMin normalization

```java
int cdfMin = 0;

for (int i = 0; i < 256; i++) {
    if (cumulative[i] != 0) {
        cdfMin = cumulative[i];
        break;
    }
}

double cdf = (double) (cumulative[lum] - cdfMin) / (double) (total_pixels - cdfMin);
int newLum = (int) Math.round(255.0 * cdf);
```
