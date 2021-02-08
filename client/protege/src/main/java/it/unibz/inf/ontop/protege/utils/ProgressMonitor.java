package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.*;

public class ProgressMonitor {

    private final Component parent;
    private final String cancelOption;
    private final Object message;
    private final boolean indeterminate;

    private JDialog dialog;
    private JOptionPane pane;
    private JProgressBar progressBar;
    private JLabel noteLabel;

    private boolean isDone;

    private volatile boolean isCancelled = false;
    private volatile boolean isCancellable = true;

    public ProgressMonitor(Component parent, Object message, boolean indeterminate) {
        this.parent = parent;
        this.message = message;
        this.indeterminate = indeterminate;

        this.cancelOption = UIManager.getString("OptionPane.cancelButtonText");
    }

    private Runnable cancelAction;

    public void setCancelAction(Runnable cancelAction) {
        this.cancelAction = cancelAction;
    }

    public void open(String note) {
        if (dialog == null && !isDone && !isCancelled) {
            noteLabel = new JLabel(note, null, SwingConstants.CENTER);

            progressBar = new JProgressBar();
            if (indeterminate) {
                progressBar.setIndeterminate(true);
            }
            else {
                progressBar.setMinimum(0);
                progressBar.setMaximum(100);
                progressBar.setValue(0);
            }

            pane = new JOptionPane(new Object[] { message, noteLabel, progressBar },
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    IconLoader.getOntopIcon(),
                    new Object[] { cancelOption },
                    null);

            dialog = pane.createDialog(parent, UIManager.getString("ProgressMonitor.progressText"));
            dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            pane.addPropertyChangeListener(evt -> {
                if (evt.getSource() == pane
                        && evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
                        && cancelOption.equals(evt.getNewValue())) {

                    if (cancel()) {
                        noteLabel.setText("cancelling...");
                        pane.setEnabled(false);
                        if (cancelAction != null)
                            cancelAction.run();
                    }
                    dialog.setVisible(true);
                }
            });

            dialog.setResizable(true);
            dialog.setVisible(true);
        }
    }

    public void close() {
        isDone = true;
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }

    private synchronized boolean cancel() {
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

    public void setProgress(int percentage, String note) {
        if (dialog != null && !isCancelled) {
            if (!indeterminate) {
                progressBar.setValue(percentage);
            }
            noteLabel.setText(note);
        }
    }
}

