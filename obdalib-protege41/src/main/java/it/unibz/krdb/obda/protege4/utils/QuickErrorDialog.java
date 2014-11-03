/**
 * Used by DialogUtils.java. 
 * Creates an error message dialog box with some extra info about call stack etc.
 * Put into a separate Runnable to facilitate sending to the SwingUtilities.invokeLater for
 * proper thread-safe execution
 */

package it.unibz.krdb.obda.protege4.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class QuickErrorDialog implements Runnable{
	Component parent;
	Exception e;
	String message;
	
	QuickErrorDialog(Component parent, Exception e, String message){
		super();
		this.parent = parent;
		this.e = e;
		this.message = message;
	}
	
	public void run(){
		// create and configure a text area - fill it with exception text.
		final JTextArea textArea = new JTextArea();
		textArea.setBackground(Color.WHITE);
		textArea.setFont(new Font("Monaco", Font.PLAIN, 11));
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		StringWriter writer = new StringWriter();
		writer.write(e.getLocalizedMessage());
		writer.write("\n\n");
		writer.write("###################################################\n");
		writer.write("##    Debugging information (for the authors)    ##\n");
		writer.write("###################################################\n\n");

		StackTraceElement[] elemnts = e.getStackTrace();
		for (int i = 0; i < elemnts.length; i++) {
			writer.write("\tat " + elemnts[i].toString() + "\n");
		}

		textArea.setText(writer.toString());
		textArea.setCaretPosition(0);

		// stuff it in a scrollpane with a controlled size.
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(800, 450));

		// pass the scrollpane to the joptionpane.
		JOptionPane.showMessageDialog(parent, scrollPane, message, JOptionPane.ERROR_MESSAGE);
	}
}