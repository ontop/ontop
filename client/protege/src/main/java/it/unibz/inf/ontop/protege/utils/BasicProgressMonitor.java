package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * all methods are called on the worker thread, except
 *     - close, which can also be called on the event dispatch thread
 *     - cancel, which is always called on the event dispatch thread
 */

public class BasicProgressMonitor {

    /**
        possible transitions:
            INIT -openIfPossible-> LIVE
            LIVE -makeFinalIfPossible-> COMPLETED

            INIT -cancelIfPossible-> CANCELLED
            LIVE -cancelIfPossible-> CANCELLED

            INIT -close-> CLOSED
            COMPLETED -close-> CLOSED
            CANCELLED -close-> CLOSED
            CLOSED -close-> CLOSED
            LIVE -close-> CLOSED (after any exception in the worker thread)
     */

    private static final int INIT = 0;
    private static final int LIVE = 1;
    private static final int COMPLETED = 2;
    private static final int CANCELLED = 3;
    private static final int CLOSED = 4;

    private volatile int state = INIT;

    private Runnable cancelAction;

    public void setCancelAction(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    private final ProgressMonitorComponent component;

    public BasicProgressMonitor(ProgressMonitorComponent component) {
        this.component = component;
    }

    public ProgressMonitorComponent getComponent() {
        return component;
    }

    /**
     * called on worker thread - posts the opening event on the event queue
     *
     * @param status
     */
    public boolean open(String status) {
        synchronized (this) {
            if (state != INIT)
                return false;

            state = LIVE;
        }
        SwingUtilities.invokeLater(() -> component.onOpen(status, this));
        return true;
    }

    public boolean isCancelled() {
        return state == CANCELLED;
    }

    public boolean isLive() {
        return state == LIVE;
    }

    /**
     * called on the worker thread - posts update on the event queue
     *
     * @param percentage
     * @param status
     */
    public final void setProgress(int percentage, String status) {
        if (!isCancelled())
            SwingUtilities.invokeLater(() -> component.onProgress(percentage, status));
    }

    /**
     * called on worker thread - waits until the UI is synchronised
     * @param status
     * @return
     */

    public final boolean makeFinal(String status) {
        synchronized (this) {
            if (state != LIVE)
                return false;

            state = COMPLETED;
        }
        try {
            // wait until the message is displayed
            SwingUtilities.invokeAndWait(() -> component.onMakeFinal(status));
        }
        catch (InterruptedException | InvocationTargetException ignore) {
        }
        return true;
    }

    /**
     * called both on worker thread and event dispatch thread
     */

    public final void close() {
        state = CLOSED;
        SwingUtilities.invokeLater(component::onClose);
    }

    /**
     * called on the event dispatch thread
     *
     * @return true if cancelled
     */

    protected final boolean cancel() {

        synchronized (this) {
            if (state != INIT && state != LIVE)
                return false;

            state = CANCELLED;
        }

        component.onCancel();
        if (cancelAction != null) {
            Thread cancellationThread = new Thread(cancelAction);
            cancellationThread.start();
        }
        return true;
    }
}
