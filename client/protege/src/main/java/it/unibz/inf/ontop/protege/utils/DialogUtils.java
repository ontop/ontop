package it.unibz.inf.ontop.protege.utils;

/*
 * #%L
 * ontop-protege
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;

public class DialogUtils {

	public static void showQuickErrorDialog(Component parent, Exception e) {
		showQuickErrorDialog(parent, e, "An Error Has Occurred");
	}

	public static void open(URI uri, Component component) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(uri);
			} catch (IOException e) {
				DialogUtils.showQuickErrorDialog(component, e);
			}
		} else {
			JOptionPane.showMessageDialog(component, "URL links are not supported in this Desktop", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static synchronized void showQuickErrorDialog(Component parent, Exception e, String message) {
		QuickErrorDialog box = new QuickErrorDialog(parent, e, message);
		SwingUtilities.invokeLater(box);
	}

	public static void centerDialogWRTParent(Component parent, Component dialog) {
		Container myParent = (Container) parent;
		Point topLeft = myParent.getLocationOnScreen();
		Dimension parentSize = myParent.getSize();
		Dimension mySize = dialog.getSize();

		int x = (parentSize.width > mySize.width)
				? ((parentSize.width - mySize.width) / 2) + topLeft.x
				: topLeft.x;

		int y = (parentSize.height > mySize.height)
				? ((parentSize.height - mySize.height) / 2) + topLeft.y
				: topLeft.y;

		dialog.setLocation(x, y);
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
