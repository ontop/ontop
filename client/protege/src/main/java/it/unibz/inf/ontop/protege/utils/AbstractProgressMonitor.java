package it.unibz.inf.ontop.protege.utils;

public abstract class AbstractProgressMonitor {
    private boolean isDone;

    private volatile boolean isCancelled = false;
    private volatile boolean isCancellable = true;

    private Runnable cancelAction;

    public void setCancelAction(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    protected void proceedCancelling() {
        setStatus("cancelling...");
        if (cancelAction != null) {
            Thread cancellationThread = new Thread(cancelAction);
            cancellationThread.start();
        }
    }

    protected synchronized boolean cancelIfPossible() {
        if (isCancellable)
            isCancelled = true;

        return isCancelled;
    }

    private synchronized void makeFinal() {
        if (!isCancelled)
            isCancellable = false;
    }

    public void prepareClosing(String note) {
        makeFinal();
        if (isCancelled)
            close();
        else
            setProgress(100, note);
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public boolean isDone() {
        return isDone;
    }

    public void close() {
        isDone = true;
    }

    public void open(String status) {
        setStatus(status);
    }

    public abstract void setProgress(int percentage, String status);

    public abstract void setStatus(String status);
}
