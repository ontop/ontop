package it.unibz.inf.ontop.protege.utils;

import java.awt.*;

public abstract class SwingWorkerWithCompletionPercentageMonitor<T, V> extends SwingWorkerWithMonitor<T, V> {

    private int count, max;

    protected SwingWorkerWithCompletionPercentageMonitor(Component parent, Object message) {
        super(new ProgressMonitorDialogComponent(parent, message, false));
    }

    @Override
    protected void tick() throws CancelActionException {
        count++;
        notifyProgressMonitor();
        super.tick();
    }

    protected void setMaxTicks(int max) {
        this.max = max;
    }

    public int getCompletionPercentage() {
        if (max > 0)
            return (int) (count * 100.0 / max);

        return 0;
    }
}
