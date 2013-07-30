/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

public class SelectPrefixPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = -8277829841902027620L;

	private Map<String, String> prefixMap = null;
	private JDialog parent = null;
	private JTextPane querypane = null;
	private Vector<JCheckBox> checkboxes = null;

	/** 
	 * Creates new form SelectPrefixDialog 
	 */
	public SelectPrefixPanel(PrefixManager manager, JTextPane parent) {
		super();

		// Cloning the existing manager
		PrefixManager prefManClone = new SimplePrefixManager();
		Map<String, String> currentMap = manager.getPrefixMap();
		for (String prefix : currentMap.keySet()) {
			prefManClone.addPrefix(prefix, manager.getURIDefinition(prefix));
		}
		prefixMap = prefManClone.getPrefixMap();
		querypane = parent;

		// Adding predefined prefixes
		boolean containsXSDPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_XSD);
		boolean containsRDFPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_RDF);
		boolean containsRDFSPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_RDFS);
		boolean containsOWLPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_OWL);
		boolean containsQUESTPrefix = prefManClone.contains(OBDAVocabulary.PREFIX_QUEST);

		if (!containsXSDPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_XSD, OBDAVocabulary.NS_XSD);
		}
		if (!containsRDFPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_RDF, OBDAVocabulary.NS_RDF);
		}
		if (!containsRDFSPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_RDFS, OBDAVocabulary.NS_RDFS);
		}
		if (!containsOWLPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_OWL, OBDAVocabulary.NS_OWL);
		}
		if (!containsQUESTPrefix) {
			prefManClone.addPrefix(OBDAVocabulary.PREFIX_QUEST, OBDAVocabulary.NS_QUEST);
		}
		initComponents();
		drawCheckBoxes();
	}

	public void show() {
		parent = new JDialog();
		parent.setModal(true);
		parent.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		parent.setContentPane(this);
		parent.setLocationRelativeTo(querypane);
		parent.pack();
		jButtonAccept.requestFocus();

		DialogUtils.installEscapeCloseOperation(parent);
		DialogUtils.centerDialogWRTParent(querypane, parent);
		parent.setVisible(true);
	}

	private boolean isBasePrefix(String prefix) {
		return (prefix.equals(":")) ? true : false;
	}

	private void drawCheckBoxes() {
		checkboxes = new Vector<JCheckBox>();
		int gridYIndex = 1;
		for (String key : prefixMap.keySet()) {
			if (!key.equals("version")) {
				jCheckBox1 = new JCheckBox();
				jCheckBox1.setText(key);
				jCheckBox1.setPreferredSize(new Dimension(444, 18));
				jCheckBox1.setFont(new java.awt.Font("Tahoma", 1, 11));
				jCheckBox1.setPreferredSize(new Dimension(75, 15));
				java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
				gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
				gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
				gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
				gridBagConstraints.gridx = 0;
				gridBagConstraints.gridy = isBasePrefix(key) ? 0 : gridYIndex;
				gridBagConstraints.weightx = 0.0;
				jPanel2.add(jCheckBox1, gridBagConstraints);

				jLabel1 = new JLabel();
				jLabel1.setText("<" + prefixMap.get(key) + ">");
				jLabel1.setPreferredSize(new Dimension(350, 15));
				gridBagConstraints = new java.awt.GridBagConstraints();
				gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
				gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
				gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
				gridBagConstraints.gridx = 1;
				gridBagConstraints.gridy = isBasePrefix(key) ? 0 : gridYIndex;
				gridBagConstraints.weightx = 1.0;
				jPanel2.add(jLabel1, gridBagConstraints);

				// Arrange the check-box list
				if (isBasePrefix(key)) {
					// Always put the base prefix on top of the list.
					checkboxes.add(0, jCheckBox1);
				} else {
					checkboxes.add(jCheckBox1);
				}
				// Increase the index Y counter
				gridYIndex++;
			}
		}

		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = gridYIndex + 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jPanel2.add(jPanel3, gridBagConstraints);

		jButtonCancel.setToolTipText("Cancel the attachment of prefixes. (ESCAPE)");
		jButtonCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		jButtonSelectAll.setToolTipText("Select all shown prefixes. (CTRL+A)");
		jButtonSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAll();
			}
		});
		jButtonSelectAll.setToolTipText("Unselect all shown prefixes. (CTRL+N)");
		jButtonSelectNone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectNone();
			}
		});
		jButtonAccept.setToolTipText("Add selected prefixes to query. (ENTER)");
		jButtonAccept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				accept();
			}
		});

		ActionListener actionListenerCancel = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				cancel();
			}
		};
		KeyStroke ks_ecape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		this.registerKeyboardAction(actionListenerCancel, ks_ecape, JComponent.WHEN_IN_FOCUSED_WINDOW);

		jButtonAccept.requestFocusInWindow();

		ActionListener actionListenerAccept = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				accept();
			}
		};
		KeyStroke ks_enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		this.registerKeyboardAction(actionListenerAccept, ks_enter, JComponent.WHEN_IN_FOCUSED_WINDOW);

		jButtonCancel.setMnemonic('c');
		jButtonSelectAll.setMnemonic('a');
		jButtonSelectNone.setMnemonic('n');
		jButtonAccept.setPreferredSize(new Dimension(80, 22));
		jButtonCancel.setPreferredSize(new Dimension(80, 22));
		jButtonSelectAll.setPreferredSize(new Dimension(80, 22));
		jButtonSelectNone.setPreferredSize(new Dimension(80, 22));
		jPanel3.setPreferredSize(new Dimension(1, 1));
	}

	private void selectAll() {
		for (JCheckBox box : checkboxes) {
			box.setSelected(true);
		}
	}

	private void selectNone() {
		for (JCheckBox box : checkboxes) {
			box.setSelected(false);
		}
	}

	private void accept() {
		StringBuffer directive = new StringBuffer();
		for (JCheckBox checkbox : checkboxes) {
			if (checkbox.isSelected()) {
				String prefix = checkbox.getText();
				directive.append("PREFIX ");
				directive.append(prefix);
				directive.append(" ");
				directive.append("<");
				directive.append(prefixMap.get(prefix));
				directive.append(">");
				directive.append("\n");
			}
		}
		String currentcontent = querypane.getText();
		String newContent = directive + currentcontent;
		querypane.setText(newContent);
		parent.setVisible(false);
		parent.dispose();
	}

	private void cancel() {
		parent.setVisible(false);
		parent.dispose();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jLabelHeader = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		jButtonAccept = new javax.swing.JButton();
		jButtonSelectAll = new javax.swing.JButton();
		jButtonSelectNone = new javax.swing.JButton();
		jButtonCancel = new javax.swing.JButton();
		jPanel2 = new javax.swing.JPanel();
		jCheckBox1 = new javax.swing.JCheckBox();
		jPanel3 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();

		setMinimumSize(new java.awt.Dimension(100, 100));
		setLayout(new java.awt.GridBagLayout());

		jLabelHeader.setText("Select the prefixes relevant for your query:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(jLabelHeader, gridBagConstraints);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		jButtonAccept.setText("Accept");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel1.add(jButtonAccept, gridBagConstraints);

		jButtonSelectAll.setText("Select All");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel1.add(jButtonSelectAll, gridBagConstraints);

		jButtonSelectNone.setText("Select None");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel1.add(jButtonSelectNone, gridBagConstraints);

		jButtonCancel.setText("Cancel");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
		jPanel1.add(jButtonCancel, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(jPanel1, gridBagConstraints);

		jPanel2.setLayout(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jPanel2.add(jPanel3, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		add(jPanel2, gridBagConstraints);
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButtonAccept;
	private javax.swing.JButton jButtonCancel;
	private javax.swing.JButton jButtonSelectAll;
	private javax.swing.JButton jButtonSelectNone;
	private javax.swing.JCheckBox jCheckBox1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabelHeader;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	// End of variables declaration//GEN-END:variables
}
