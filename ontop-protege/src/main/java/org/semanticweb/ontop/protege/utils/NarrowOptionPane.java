package org.semanticweb.ontop.protege.utils;

import javax.swing.*;

/**
 * Set size of JOptionPane, so that the warning message renaming visible on the screen
 * Created by Sarah on 21/03/16.
 */
public class NarrowOptionPane extends JOptionPane {

    NarrowOptionPane() {

    }

    public int getMaxCharactersPerLineCount() {
        return 600;
    }


}
