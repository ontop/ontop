package it.unibz.inf.ontop.protege.utils;


import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * the sequence of the calls on the worker thread should be as follows:
 * start - startLoop - (tick* | notifyProgressMonitor)* - endLoop - end
 *
 * @param <T>
 * @param <V>
 */

public abstract class SwingWorkerWithMonitor<T, V> extends SwingWorker<T, V> {

    protected final BasicProgressMonitor progressMonitor;
    protected final long startTime;

    private Supplier<String> statusSupplier;
    private Supplier<Integer> progressSupplier;

    protected SwingWorkerWithMonitor(Component parent, Object message, boolean indeterminate) {
        this(new ProgressMonitorDialogComponent(parent, message, indeterminate));
    }

    protected SwingWorkerWithMonitor(ProgressMonitorComponent component) {
        this.progressMonitor = new BasicProgressMonitor(component);
        this.startTime = System.currentTimeMillis();
    }

    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }


    /**
     * called on the worker thread
     * @param status
     */
    protected void start(String status) {
        progressMonitor.open(status);
    }

    /**
     * called on the worker thread
     *
     * @param progressSupplier
     * @param statusSupplier
     * @throws CancelActionException
     */
    protected void startLoop(Supplier<Integer> progressSupplier, Supplier<String> statusSupplier) throws CancelActionException {
        terminateIfCancelled();
        this.statusSupplier = statusSupplier;
        this.progressSupplier = progressSupplier;
        notifyProgressMonitor();
    }

    /**
     * called on the worker thread
     *
     * @throws CancelActionException
     */
    protected void tick() throws CancelActionException {
        terminateIfCancelled();
    }

    /**
     * called on the worker thread
     */
    protected void notifyProgressMonitor() {
        progressMonitor.setProgress(progressSupplier.get(), statusSupplier.get());
    }


    /**
     * called on the worker thread
     *
     * @param endLoopMessage
     * @throws CancelActionException
     */
    protected void endLoop(String endLoopMessage) throws CancelActionException {
        progressMonitor.makeFinal(endLoopMessage);
        if (progressMonitor.isCancelled())
            throw new CancelActionException();
    }

    /**
     * called on the worker thread
     */
    protected void end() {
        progressMonitor.close();
    }

    /**
     * called on the event dispatch thread
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */

    protected T complete() throws ExecutionException, InterruptedException {
        try {
            return get();
        }
        catch (ExecutionException e) {
            if (e.getCause() instanceof CancelActionException)
                throw new CancellationException();
            throw e;
        }
        finally {
            progressMonitor.close(); // in case doInBackground threw an exception
        }
    }

    private void terminateIfCancelled() throws CancelActionException {
        if (progressMonitor.isCancelled()) {
            progressMonitor.close();
            throw new CancelActionException();
        }
    }

    public static class CancelActionException extends Exception { }

}
