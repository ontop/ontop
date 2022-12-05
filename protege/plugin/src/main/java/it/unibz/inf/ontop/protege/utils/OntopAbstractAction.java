package it.unibz.inf.ontop.protege.utils;

import javax.swing.*;

public abstract class OntopAbstractAction extends AbstractAction {
    public OntopAbstractAction(String name, String icon, String tooltip, KeyStroke accelerator) {
        super(name);
        putValue(SMALL_ICON, DialogUtils.getImageIcon("images/" + icon));
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(ACCELERATOR_KEY, accelerator);
    }

    public void setAccelerator(int keyCode, int modifiers) {
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyCode, modifiers));
    }

    public String getName() { return (String)getValue(NAME); }
    public ImageIcon getIcon() { return (ImageIcon)getValue(SMALL_ICON); }

    public String getTooltip() { return (String)getValue(SHORT_DESCRIPTION); }

    public KeyStroke getAccelerator() { return (KeyStroke)getValue(ACCELERATOR_KEY); }
}
