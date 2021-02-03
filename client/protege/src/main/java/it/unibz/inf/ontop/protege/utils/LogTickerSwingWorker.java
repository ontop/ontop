package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;

public abstract class LogTickerSwingWorker<T, V> extends TickerSwingWorker<T, V> {

    private final long startTime;
    private long previousTime;
    private int count, progress = 1;

    protected LogTickerSwingWorker(ProgressMonitor progressMonitor) {
        super(progressMonitor);
        previousTime = startTime = System.currentTimeMillis();
    }

    @Override
    protected void tick() throws CancelActionException {
        count++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - previousTime > 100) {
//            if (currentTime - startTime > Math.pow(2, progress / 4.0)) {
                previousTime = currentTime;
            SwingUtilities.invokeLater(() -> progressMonitor.setProgress(0, ""  + count));
//                progress++;
//                setProgress(progress);
//            }
        }
        checkCancelled();
    }

    public int getCount() {
        return count;
    }

    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }
}
