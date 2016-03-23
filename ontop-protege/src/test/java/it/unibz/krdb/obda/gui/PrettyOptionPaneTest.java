package it.unibz.krdb.obda.gui;

import org.semanticweb.ontop.protege.utils.OptionPaneUtils;

import javax.swing.*;

public class PrettyOptionPaneTest {

    public static void main(String[] args) {

        String longMessage = "Text is super long and it will take a few lines to get everything that is written here. " +
                "It looks really horrible when the default JOption pane is used. " +
                "It is is better to create a PrettyOptionPane that reduces the default size of the window and makes it looks really pretty. ";

        // create a jframe
        JFrame frame = new JFrame("JOptionPane showMessageDialog example");

        // show a joptionpane dialog using showMessageDialog
        OptionPaneUtils.showMessageDialog(frame, longMessage, "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
}