package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public abstract class TickerSwingWorker<T, V> extends SwingWorker<T, V> {

    protected final ProgressMonitor progressMonitor;

    private PropertyChangeListener listener;

    protected TickerSwingWorker(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    protected void start(String startProgressNote) {
        SwingUtilities.invokeLater(() ->
                progressMonitor.open(startProgressNote));
    }

    protected void startLoop(Supplier<String> renderer) throws CancelActionException {
        checkCancelled();
        listener = evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                int progress = (Integer) evt.getNewValue();
                progressMonitor.setProgress(progress, renderer.get());
            }
        };
        addPropertyChangeListener(listener);
        SwingUtilities.invokeLater(() ->
                progressMonitor.setProgress(0, renderer.get()));
    }

    protected void endLoop(String endLoopMessage) throws CancelActionException {
        removePropertyChangeListener(listener);
        try {
            SwingUtilities.invokeAndWait(() ->
                progressMonitor.setProgress(99, endLoopMessage));
        }
        catch (InterruptedException | InvocationTargetException e) {
            /* NO-OP */
        }

        synchronized (progressMonitor) {
            if (!progressMonitor.isCancelled())
                progressMonitor.end();
        }
        checkCancelled();
    }

    protected void end() {
        try {
            // waiting allows Protege to update UI with any ontology changes
            // the Protege updates in the queue precede the closing event
            SwingUtilities.invokeAndWait(progressMonitor::close);
        }
        catch (InterruptedException | InvocationTargetException e) {
            /* NO-OP */
        }
    }

    protected T complete() throws ExecutionException, InterruptedException {
        progressMonitor.close(); // in case doInBackground threw an exception
        try {
            return get();
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof CancelActionException)
                throw new CancellationException();
            throw e;
        }
    }

    abstract protected void tick() throws CancelActionException;

    protected void checkCancelled() throws CancelActionException {
        if (progressMonitor.isCancelled()) {
            end();
            throw new CancelActionException();
        }
    }

    protected static class CancelActionException extends Exception { }
}
