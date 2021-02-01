package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;

public abstract class TickerSwingWorker<T, V> extends SwingWorker<T, V> {

    abstract public String getProgressNote();

    protected TickerSwingWorker(ProgressMonitor progressMonitor) {

        addPropertyChangeListener(evt -> {
            System.out.println("EVENT " + evt + " STATE " + getState());
            if (progressMonitor.isCanceled()) {
                cancel(false);
            }
            else if (isDone()) {
                progressMonitor.setProgress(100);
            }
            else if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                progressMonitor.setProgress(progress);
                progressMonitor.setNote(getProgressNote());
            }
        });
    }

    abstract protected boolean tick();
}
