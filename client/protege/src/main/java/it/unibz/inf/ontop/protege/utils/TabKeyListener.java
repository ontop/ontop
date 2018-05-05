package it.unibz.inf.ontop.protege.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TabKeyListener implements KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {
        typedOrPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // if (e.getKeyCode() == KeyEvent.VK_TAB) {
        // e.getComponent().transferFocus();
        // e.consume();
        // }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        typedOrPressed(e);
    }

    private void typedOrPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_TAB) {
            if (e.getModifiers() == KeyEvent.SHIFT_MASK) {
                e.getComponent().transferFocusBackward();
            } else {
                e.getComponent().transferFocus();
            }
            e.consume();
        }
    }

}
