package it.unibz.inf.ontop.protege.utils;

/**
 * all methods are called on the event dispatch thread
 */

public interface ProgressMonitorComponent {

    void onOpen(String status, BasicProgressMonitor progressMonitor);

    void onProgress(int percentage, String status);

    void onMakeFinal(String status);

    void onClose();

    void onCancel();
}
