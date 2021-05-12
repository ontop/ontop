package it.unibz.inf.ontop.protege.utils;


import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@FunctionalInterface
public interface SimpleDocumentListener extends DocumentListener {

    @Override
    default void insertUpdate(DocumentEvent e) {
        change(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        change(e);
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        change(e);
    }

    void change(DocumentEvent e);
}
