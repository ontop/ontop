package it.unibz.inf.ontop.protege.utils;


import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public abstract class SwingWorkerWithMonitor<T, V> extends SwingWorker<T, V> {

    private static final int DELAY_OPENING_WINDOW = 0; //300;

    protected final AbstractProgressMonitor progressMonitor;
    protected final long startTime;

    private PropertyChangeListener listener;
    private Supplier<String> statusSupplier;
    private Supplier<Integer> progressSupplier;

    protected SwingWorkerWithMonitor(Component parent, Object message, boolean indeterminate) {
        this(() -> new ProgressMonitor(parent, message, indeterminate));
    }

    protected SwingWorkerWithMonitor(Supplier<AbstractProgressMonitor> progressMonitorConstructor) {
        this.progressMonitor = progressMonitorConstructor.get();
        this.startTime = System.currentTimeMillis();
    }

    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    protected void start(String startProgressNote) {
        // executes the action on the event-dispatch thread
        Timer timer = new Timer(DELAY_OPENING_WINDOW, e -> progressMonitor.open(startProgressNote));
        timer.setRepeats(false);
        timer.start();
    }

    protected void startLoop(Supplier<Integer> progressSupplier, Supplier<String> statusSupplier) throws CancelActionException {
        terminateIfCancelled();
        this.statusSupplier = statusSupplier;
        this.progressSupplier = progressSupplier;
        this.listener = evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressMonitor.setProgress(progressSupplier.get(), statusSupplier.get());
            }
        };
        addPropertyChangeListener(listener);
        notifyProgressMonitor();
    }

    protected void notifyProgressMonitor() {
        SwingUtilities.invokeLater(() ->
                progressMonitor.setProgress(progressSupplier.get(), statusSupplier.get()));
    }

    protected void endLoop(String endLoopMessage) throws CancelActionException {
        removePropertyChangeListener(listener);
        try {
            SwingUtilities.invokeAndWait(() -> progressMonitor.prepareClosing(endLoopMessage));
        }
        catch (InterruptedException | InvocationTargetException e) {
            /* NO-OP */
        }
        if (progressMonitor.isCancelled())
            throw new CancelActionException();
    }

    protected void end() {
        closeProgressMonitorAndWait();
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

    protected void tick() throws CancelActionException {
        terminateIfCancelled();
    }

    private void terminateIfCancelled() throws CancelActionException {
        if (progressMonitor.isCancelled()) {
            closeProgressMonitorAndWait();
            throw new CancelActionException();
        }
    }

    protected static class CancelActionException extends Exception { }

    private void closeProgressMonitorAndWait() {
        try {
            // waiting allows Protege to update UI with any ontology changes
            // the Protege updates in the queue precede the closing event
            SwingUtilities.invokeAndWait(progressMonitor::close);
        }
        catch (InterruptedException | InvocationTargetException e) {
            /* NO-OP */
        }
    }
}
