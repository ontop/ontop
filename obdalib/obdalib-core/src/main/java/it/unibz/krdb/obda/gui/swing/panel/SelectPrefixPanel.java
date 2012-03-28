/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.unibz.krdb.obda.gui.swing.panel;

import it.unibz.krdb.obda.gui.swing.utils.DialogUtils;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

/*
 * SelectPrefixDialog.java
 *
 * Created on 22-mar-2011, 10.54.46
 */

/**
 * 
 * @author Manfred Gerstgrasser
 */
public class SelectPrefixPanel extends javax.swing.JPanel {

	private Map<String, String> prefixMap = null;
	private JDialog parent = null;
	private JTextPane querypane = null;
	private Vector<JCheckBox> checkboxes = null;
	private Vector<JLabel> labels = null;
	private String base = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = -8277829841902027620L;

	/** Creates new form SelectPrefixDialog */
	public SelectPrefixPanel(Map<String, String> prefixes, JTextPane pane, String base) {
		super();
		this.prefixMap = prefixes;
		this.querypane = pane;
		this.base = base;
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
		labels = new Vector<JLabel>();
		int yIndex = 1;
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
				gridBagConstraints.gridy = isBasePrefix(key) ? 0 : yIndex;
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
				gridBagConstraints.gridy = isBasePrefix(key) ? 0 : yIndex;
				gridBagConstraints.weightx = 1.0;
				jPanel2.add(jLabel1, gridBagConstraints);
				labels.add(jLabel1);
				
				// Arrange the check-box list
				if (isBasePrefix(key)) {
					// Always put the base prefix on top of the list.
					checkboxes.add(0, jCheckBox1);
					labels.add(0, jLabel1);
				} else {
					checkboxes.add(jCheckBox1);
					labels.add(jLabel1);
				}
				
				// Increase the index Y counter
				yIndex++;
			}
		}

		java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = yIndex + 1;
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
		StringBuffer prefix = new StringBuffer();
		int i = 0;
		for (JCheckBox checkbox : checkboxes) {
			if (checkbox.isSelected()) {
				prefix.append("PREFIX ");
				prefix.append(checkbox.getText());
				prefix.append(" ");
				prefix.append(labels.get(i).getText());
				prefix.append("\n");
			}
			i++;
		}
		String currentcontent = querypane.getText();
		String newContent = prefix + currentcontent;
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
