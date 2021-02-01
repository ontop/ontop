package it.unibz.inf.ontop.protege.utils;

public abstract class LinearTickerSwingWorker<T, V> extends TickerSwingWorker<T, V> {

    private int count, max;

    protected LinearTickerSwingWorker(ProgressMonitor progressMonitor) {
        super(progressMonitor);
    }

    @Override
    protected boolean tick() {
        count++;
        setProgress(getCompletionPercentage());
        return isCancelled();
    }

    public int getCount() {
        return count;
    }

    protected void setMaxTicks(int max) {
        this.max = max;
    }

    public int getCompletionPercentage() {
        if (max > 0)
            return (int) ((count + 1) * 100.0 / max);

        return 0;
    }
}
