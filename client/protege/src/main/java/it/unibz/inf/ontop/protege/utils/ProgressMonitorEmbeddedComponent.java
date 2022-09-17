package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ProgressMonitorEmbeddedComponent implements ProgressMonitorComponent {

    private final JButton startButton, stopButton;
    private final JLabel statusLabel;
    private ActionListener cancelListener;

    public ProgressMonitorEmbeddedComponent(JButton startButton, JButton stopButton, JLabel statusLabel) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.statusLabel = statusLabel;
    }

    @Override
    public void onOpen(String status, BasicProgressMonitor progressMonitor) {
        this.cancelListener = evt -> progressMonitor.cancel();
        stopButton.addActionListener(cancelListener);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText(status);
    }

    @Override
    public void onProgress(int percentage, String status) {
        statusLabel.setText(status);
    }

    @Override
    public void onMakeFinal(String status) {
        stopButton.setEnabled(false);
    }

    @Override
    public void onClose() {
        if (cancelListener != null)
            stopButton.removeActionListener(cancelListener);
        cancelListener = null;
        stopButton.setEnabled(false);
        startButton.setEnabled(true);
    }

    @Override
    public void onCancel() {
        stopButton.setEnabled(false);
        statusLabel.setText("cancelling...");
    }
}
