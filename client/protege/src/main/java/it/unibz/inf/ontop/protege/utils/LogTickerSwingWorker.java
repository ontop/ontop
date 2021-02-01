package it.unibz.inf.ontop.protege.utils;

public abstract class LogTickerSwingWorker<T, V> extends TickerSwingWorker<T, V> {

    private final long startTime;
    private long previousTime;
    private int count, progress = 1;

    protected LogTickerSwingWorker(ProgressMonitor progressMonitor) {
        super(progressMonitor);
        previousTime = startTime = System.currentTimeMillis();
    }

    @Override
    protected boolean tick() {
        count++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - previousTime > 200) {
            if (currentTime - startTime > Math.pow(2, progress / 4.0)) {
                previousTime = currentTime;
                progress++;
                setProgress(progress);
            }
            return isCancelled();
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }
}
