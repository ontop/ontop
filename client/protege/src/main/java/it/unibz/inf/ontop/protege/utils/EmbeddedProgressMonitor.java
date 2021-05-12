package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmbeddedProgressMonitor extends AbstractProgressMonitor implements ActionListener {

    private final JButton startButton, stopButton;
    private final JLabel statusLabel;

    public EmbeddedProgressMonitor(JButton startButton, JButton stopButton, JLabel statusLabel) {
        this.startButton = startButton;
        this.stopButton = stopButton;
        this.statusLabel = statusLabel;
    }

    @Override
    public void setProgress(int percentage, String status) {
        statusLabel.setText(status);
    }

    @Override
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    @Override
    public void close() {
        super.close();
        stopButton.setEnabled(false);
        stopButton.removeActionListener(this);
        startButton.setEnabled(true);
    }

    @Override
    public void open(String status) {
        startButton.setEnabled(false);
        stopButton.addActionListener(this);
        stopButton.setEnabled(true);
        super.open(status);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (cancelIfPossible()) {
            stopButton.setEnabled(false);
            proceedCancelling();
        }
    }
}
