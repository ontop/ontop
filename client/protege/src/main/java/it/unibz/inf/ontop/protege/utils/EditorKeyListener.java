package it.unibz.inf.ontop.protege.utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EditorKeyListener implements KeyListener {
    private EditorPanel editorPanel;
    public EditorKeyListener(EditorPanel _editorPanel){
        editorPanel = _editorPanel;
    }
    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                editorPanel.remove();
                break;
            case KeyEvent.VK_INSERT:
                editorPanel.add();
                break;
            case KeyEvent.VK_SPACE:
                editorPanel.edit();
                break;
            default:
                break;
        }
    }
}
