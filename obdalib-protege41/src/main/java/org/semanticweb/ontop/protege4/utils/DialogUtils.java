package org.semanticweb.ontop.protege4.utils;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.semanticweb.ontop.protege4.utils.DialogUtils;

public class DialogUtils {

	public static void showQuickErrorDialog(Component parent, Exception e) {
		showQuickErrorDialog(parent, e, "An Error Has Ocurred");
	}

	public static void open(URI uri) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				DialogUtils.showQuickErrorDialog(null, e);
			}
		} else {
			JOptionPane.showMessageDialog(null, "URL links are not supported in this Desktop", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void showQuickErrorDialog(Component parent, Exception e, String message) {
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

	public static void centerDialogWRTParent(Component parent, Component dialog) {
		int x = 0;
		int y = 0;

		// Find out our parent
		Container myParent = (Container) parent;
		Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();
		Dimension mySize = dialog.getSize();

		if (parentSize.width > mySize.width) {
			x = ((parentSize.width - mySize.width) / 2) + topLeft.x;
		} else {
			x = topLeft.x;
		}
		if (parentSize.height > mySize.height) {
			y = ((parentSize.height - mySize.height) / 2) + topLeft.y;
		} else {
			y = topLeft.y;
		}
		dialog.setLocation(x, y);
	}

	public static JDialog createDialogForPanel(JDialog parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	public static JDialog createDialogForPanel(JFrame parent, JPanel panel) {
		JDialog newdialog = new JDialog(parent);
		newdialog.getContentPane().add(panel, java.awt.BorderLayout.CENTER);
		newdialog.pack();
		return newdialog;
	}

	private static final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	public static final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";

	public static void installEscapeCloseOperation(final JDialog dialog) {
		Action dispatchClosing = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent event) {
				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		};
		JRootPane root = dialog.getRootPane();
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey);
		root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);
	}
}
