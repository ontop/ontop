package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.*;

public class ProgressMonitorDialogComponent implements ProgressMonitorComponent {

    private static final int DELAY_OPENING_WINDOW = 300;

    private final Component parent;
    private final JButton cancelOption;
    private final Object message;
    private final boolean indeterminate;

    private JDialog dialog;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public ProgressMonitorDialogComponent(Component parent, Object message, boolean indeterminate) {
        this.parent = parent;
        this.message = message;
        this.indeterminate = indeterminate;

        this.cancelOption = new JButton(DialogUtils.CANCEL_BUTTON_TEXT);
    }

    @Override
    public void onOpen(String status, BasicProgressMonitor progressMonitor) {
        // executes the action on the event-dispatch thread
        Timer timer = new Timer(DELAY_OPENING_WINDOW, evt -> showDialog(status, progressMonitor));
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * called on the event dispatch thread
     * @param status
     */
    private void showDialog(String status, BasicProgressMonitor progressMonitor) {
        synchronized (progressMonitor) {
            if (!progressMonitor.isLive())
                return;

            if (dialog != null)
                throw new IllegalArgumentException("Cannot open the same monitor twice");

            statusLabel = new JLabel(status, null, SwingConstants.CENTER);

            progressBar = new JProgressBar();
            if (indeterminate) {
                progressBar.setIndeterminate(true);
            }
            else {
                progressBar.setMinimum(0);
                progressBar.setMaximum(100);
                progressBar.setValue(0);
            }

            JOptionPane pane = new JOptionPane(new Object[]{ message, statusLabel, progressBar },
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    DialogUtils.getOntopIcon(),
                    new Object[]{ cancelOption },
                    null);

            cancelOption.addActionListener(evt -> progressMonitor.cancel());

            Frame frame = getFrame(parent);
            dialog = new JDialog(frame, UIManager.getString("DialogProgressMonitor.progressText"), true);
            dialog.setContentPane(pane);
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            dialog.setResizable(true);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
        }
        dialog.setVisible(true); // HOLDS THE DISPATCH THREAD
    }

    private static Frame getFrame(Component component) {
        if (component instanceof Frame)
            return (Frame) component;

        return getFrame(component.getParent());
    }

    /**
     * called on the event dispatch thread
     * @param percentage
     * @param status
     */
    @Override
    public void onProgress(int percentage, String status) {
        if (dialog != null) {
            if (!indeterminate)
                progressBar.setValue(percentage);

            statusLabel.setText(status);
        }
    }

    @Override
    public void onMakeFinal(String status) {
        cancelOption.setEnabled(false);
        onProgress(100, status);
    }

    @Override
    public void onClose() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }

    @Override
    public void onCancel() {
        cancelOption.setEnabled(false);
        if (dialog != null)
            statusLabel.setText("cancelling...");
    }
}

