package it.unibz.inf.ontop.protege.utils;

public abstract class LinearTickerSwingWorker<T, V> extends TickerSwingWorker<T, V> {

    private int count, max;

    protected LinearTickerSwingWorker(ProgressMonitor progressMonitor) {
        super(progressMonitor);
    }

    @Override
    protected void tick() throws CancelActionException {
        count++;
        setProgress(getCompletionPercentage());
        checkCancelled();
    }

    public int getCount() {
        return count;
    }

    protected void setMaxTicks(int max) {
        this.max = max;
    }

    public int getCompletionPercentage() {
        if (max > 0)
            return (int) (count * 99.0 / max);

        return 0;
    }
}
