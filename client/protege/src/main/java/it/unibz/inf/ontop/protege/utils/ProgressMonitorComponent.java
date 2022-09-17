package it.unibz.inf.ontop.protege.utils;

import java.awt.event.ActionListener;

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
