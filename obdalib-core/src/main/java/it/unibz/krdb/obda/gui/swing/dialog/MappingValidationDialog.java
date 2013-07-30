/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.dialog;


import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MappingValidationDialog extends JDialog {

	private static final long		serialVersionUID	= -3099215805478663834L;
	private JDialog					myself				= null;
	private DefaultStyledDocument	doc					= null;
	private int						index				= 0;

	public Style					VALID				= null;
	public Style					CRITICAL_ERROR		= null;
	public Style					NONCRITICAL_ERROR	= null;
	public Style					NORMAL				= null;

	public boolean					closed				= false;

	public MappingValidationDialog(JTree tree) {

		super();
		DialogUtils.installEscapeCloseOperation(this);
		myself = this;
		doc = new DefaultStyledDocument();
		createStyles();
		createContent();
		this.setModal(true);
//		DialogUtils.centerDialogWRTParent(tree.getParent(), this);
	}

	private void createStyles() {

		StyleContext context = new StyleContext();
		VALID = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(VALID, "Arial");
		StyleConstants.setFontSize(VALID, 12);
		StyleConstants.setForeground(VALID, Color.GREEN.darker());

		StyleContext context1 = new StyleContext();
		CRITICAL_ERROR = context1.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(CRITICAL_ERROR, "Arial");
		StyleConstants.setFontSize(CRITICAL_ERROR, 12);
		StyleConstants.setForeground(CRITICAL_ERROR, Color.RED);

		StyleContext context2 = new StyleContext();
		NONCRITICAL_ERROR = context2.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(NONCRITICAL_ERROR, "Arial");
		StyleConstants.setFontSize(NONCRITICAL_ERROR, 12);
		StyleConstants.setForeground(NONCRITICAL_ERROR, Color.BLACK);

		StyleContext context3 = new StyleContext();
		NORMAL = context3.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(NONCRITICAL_ERROR, "Arial");
		StyleConstants.setFontSize(NONCRITICAL_ERROR, 11);
		StyleConstants.setForeground(NONCRITICAL_ERROR, Color.BLACK);
	}

	private void createContent() {
		this.setTitle("Validate Mapping...");
		this.setSize(new Dimension(500, 360));
		Container panel = this.getContentPane();
		panel.setLayout(new BorderLayout());
		JTextPane area = new JTextPane();
		area.setBounds(0, 0, 298, 273);
		area.setEditable(false);
		area.setBackground(Color.WHITE);
		area.setDocument(doc);
		JScrollPane areaScrollPane = new JScrollPane(area);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setBounds(0, 0, 300, 275);

		JButton button = new JButton();
		button.setText("OK");
		button.setBounds(120, 290, 60, 25);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closed = true;
				myself.dispose();
			}
		});
		button.requestFocus();

		panel.add(areaScrollPane, BorderLayout.CENTER);
		panel.add(button, BorderLayout.SOUTH);
		this.setResizable(true);
	}

	/***
	 * Adds the text synchronously. Do not call from the Event thread. Use a
	 * working thread.
	 *
	 * @param text
	 * @param style
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public void addText(final String text, final Style style) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          try {
            doc.insertString(index, text, style);
            index = index + text.length();
            invalidate();
            repaint();
          }
          catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
      });
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
	}
}
