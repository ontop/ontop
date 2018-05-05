package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public 	class PopupListener extends MouseAdapter {
    private JPopupMenu menuMappings;

    public PopupListener(JPopupMenu _menuMappings){
        menuMappings = _menuMappings;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            menuMappings.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}