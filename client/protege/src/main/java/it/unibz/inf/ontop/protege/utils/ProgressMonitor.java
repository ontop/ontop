package it.unibz.inf.ontop.protege.utils;

import it.unibz.inf.ontop.protege.gui.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressMonitor {

    private final Component parent;
    private final String cancelOption;
    private final Object message;
    private final boolean indeterminate;

    private JDialog dialog;
    private JOptionPane pane;
    private JProgressBar progressBar;
    private JLabel noteLabel;

    private String note;
    private boolean isDone;

    public ProgressMonitor(Component parent, Object message, boolean indeterminate) {
        this.parent = parent;
        this.message = message;
        this.note = "";
        this.indeterminate = indeterminate;

        this.cancelOption = UIManager.getString("OptionPane.cancelButtonText");
    }

    private void open() {
        if (dialog == null && !isCanceled() && !isDone()) {
            if (note != null)
                noteLabel = new JLabel(note, null, SwingConstants.CENTER);

            progressBar = new JProgressBar();
            if (!indeterminate) {
                progressBar.setMinimum(0);
                progressBar.setMaximum(100);
            }
            else {
                progressBar.setIndeterminate(true);
            }

            pane = new JOptionPane(new Object[]{message, noteLabel, progressBar},
                    JOptionPane.INFORMATION_MESSAGE,
                    JOptionPane.DEFAULT_OPTION,
                    IconLoader.getImageIcon("images/ontop-logo.png"),
                    new Object[]{ cancelOption },
                    null);

            dialog = pane.createDialog(parent, UIManager.getString("ProgressMonitor.progressText"));

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent we) {
                    pane.setValue(cancelOption);
                }
            });

            pane.addPropertyChangeListener(evt -> {
                if ((dialog != null) && dialog.isVisible() &&
                        evt.getSource() == pane &&
                        (evt.getPropertyName().equals(JOptionPane.VALUE_PROPERTY) ||
                                evt.getPropertyName().equals(JOptionPane.INPUT_VALUE_PROPERTY))){
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });

            dialog.setResizable(true);
            dialog.setVisible(true);
        }
    }

    private void close() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
            pane = null;
            progressBar = null;
        }
    }

    public boolean isCanceled() {
        return (pane != null) && cancelOption.equals(pane.getValue());
    }

    public boolean isDone() {
        return isDone;
    }

    public void setNote(String note) {
        this.note = note;
        if (noteLabel != null)
            noteLabel.setText(note);
    }

    public String getNote() {
        return note;
    }

    public void setProgress(int percentage) {
        if (percentage < 100) {
            open();
            if (!indeterminate && progressBar != null)
                progressBar.setValue(percentage);
        }
        else {
            isDone = true;
            close();
        }
    }
}

