package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;

public abstract class OntopAbstractAction extends AbstractAction {
    public OntopAbstractAction(String name, String icon, String tooltip) {
        super(name);
        putValue(Action.SMALL_ICON, IconLoader.getImageIcon("images/" + icon));
        putValue(Action.SHORT_DESCRIPTION, tooltip);
    }

    public void setAccelerator(int keyCode, int modifiers) {
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    public String getName() { return (String)getValue(Action.NAME); }
    public ImageIcon getIcon() { return (ImageIcon)getValue(Action.SMALL_ICON); }

    public String getTooltip() { return (String)getValue(Action.SHORT_DESCRIPTION); }
}
