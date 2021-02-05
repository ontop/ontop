package it.unibz.inf.ontop.protege.utils;

import java.awt.*;

public abstract class SwingWorkerWithTimeIntervalMonitor<T, V> extends SwingWorkerWithMonitor<T, V> {

    private final long monitorUpdateInterval;
    private long previousTime;
    private int count;

    protected SwingWorkerWithTimeIntervalMonitor(Component parent, Object message, long monitorUpdateInterval) {
        super(parent, message, true);
        this.monitorUpdateInterval = monitorUpdateInterval;
        previousTime = startTime;
    }

    @Override
    protected void tick() throws CancelActionException {
        count++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - previousTime > monitorUpdateInterval) {
            previousTime = currentTime;
            notifyProgressMonitor();
        }
        super.tick();
    }

    public int getCount() {
        return count;
    }
}
