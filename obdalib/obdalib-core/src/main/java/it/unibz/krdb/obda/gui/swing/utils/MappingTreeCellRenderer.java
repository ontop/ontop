/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingBodyNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingHeadNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingNode;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -4636107842424616156L;

	// private JLabel label = null;
	// private JTextPane area = null;
	// private JPanel panel = null;
	private OBDAPreferences pref = null;
	private final OBDAModel apic;
	private MappingTreeNodeCellRenderer editorComponent = null;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public MappingTreeCellRenderer(OBDAModel apic, OBDAPreferences preference) {
		this.apic = apic;
		pref = preference;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//		if (editorComponent == null) {
			editorComponent = new MappingTreeNodeCellRenderer(apic, pref);
//		}
		editorComponent.updateContent(node, sel, expanded, leaf, row, hasFocus);

		return editorComponent;
	}

	public class MappingTreeNodeCellRenderer extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7840766137171315837L;

		private JLabel label = null;
		private JTextPane area = null;
		private final OBDAModel apic;
		private JPanel editorComponent = null;

		Icon mappingIcon = null;
		Icon invalidmappingIcon = null;
		Icon mappingheadIcon = null;
		Icon mappingbodyIcon = null;
		Icon invalidmappingheadIcon = null;

		final String PATH_MAPPING_ICON = "images/mapping.png";
		final String PATH_INVALIDMAPPING_ICON = "images/mapping_invalid.png";

		final String PATH_MAPPINGHEAD_ICON = "images/head.png";
		final String PATH_INVALIDMAPPINGHEAD_ICON = "images/head_invalid.png";
		final String PATH_MAPPINGBODY_ICON = "images/body.png";

		final QueryPainter p; // TODO change this to OBDAPreferences later
		private final OBDAPreferences pref;

		private Boolean useDefault;

		private StyleContext context;

		private Style style;

		public MappingTreeNodeCellRenderer(OBDAModel apic, OBDAPreferences pref) {
			super();
			this.apic = apic;
			mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
			invalidmappingIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPING_ICON);
			mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
			invalidmappingheadIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPINGHEAD_ICON);
			mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);
			this.pref = pref;
			p = new QueryPainter(apic, pref);
			useDefault = new Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString());
			setupLayout();

		}

		public void updateContent(DefaultMutableTreeNode node, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			if (node instanceof MappingNode) {
				if (!useDefault) {
					StyleConstants.setFontFamily(style, pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString());
					StyleConstants.setFontSize(style, Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));
				}
				label.setIcon(mappingIcon);
				String txt = ((String) node.getUserObject());
				DefaultStyledDocument doc = new DefaultStyledDocument();
				try {
					doc.insertString(0, txt, style);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				area.setDocument(doc);

			} else if (node instanceof MappingBodyNode) {
				if (!useDefault) {
					StyleConstants.setFontFamily(style, pref.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString());
					StyleConstants.setFontSize(style, Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));
				}
				label.setIcon(mappingbodyIcon);
				String txt = ((String) node.getUserObject());
				DefaultStyledDocument doc = new DefaultStyledDocument();
				try {
					doc.insertString(0, txt, style);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				area.setDocument(doc);

			} else if (node instanceof MappingHeadNode) {

				MappingHeadNode m = (MappingHeadNode) node;
				String q = m.getQuery();

				CQIE h = parse(q);
				if (h != null)
					label.setIcon(mappingheadIcon);
				else
					label.setIcon(invalidmappingheadIcon);

				String txt = ((String) node.getUserObject());
				
				
				DefaultStyledDocument doc = new DefaultStyledDocument();

				//				MappingStyledDocument doc = new MappingStyledDocument(context, apic, pref); // TODO																									// later
				try {
//					doc.insertString(0, txt, doc.default_style);
					doc.insertString(0, txt, style);

				} catch (BadLocationException e) {
					e.printStackTrace();
				}
//				p.doRecoloring(doc);
//				while (p.isAlreadyColoring()) {
//				}
				area.setDocument(doc);

			}

			Color bg = new Color(220, 230, 240);
			if (selected) {

				setBackground(bg);
				area.setBackground(bg);
				label.setBackground(bg);
			}

		}

		public void setupLayout() {
			java.awt.GridBagConstraints grid;
			GridBagLayout l = new GridBagLayout();

			this.setLayout(l);
			label = new JLabel();
			area = new JTextPane();
			setBackground(Color.white);
			label.setBackground(Color.white);
			label.setHorizontalAlignment(SwingConstants.LEFT);
			JLabel ph = new JLabel();
			ph.setVisible(false);
			ph.setHorizontalAlignment(SwingConstants.LEADING);

			context = new StyleContext();
			style = context.getStyle(StyleContext.DEFAULT_STYLE);

			grid = new java.awt.GridBagConstraints();
			grid.gridx = 1;
			grid.gridy = 0;
			grid.gridwidth = 1;
			grid.weightx = 0;
			grid.weighty = 0;
			grid.fill = java.awt.GridBagConstraints.VERTICAL;
			add(label, grid, 0);

			grid = new java.awt.GridBagConstraints();
			grid.gridx = 2;
			grid.gridy = 0;
			grid.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			grid.fill = java.awt.GridBagConstraints.BOTH;
			grid.weightx = 1.0;
			grid.weighty = 1.0;
			add(area, grid, 1);
		}

	}

	//
	// private JPanel createComponent(DefaultMutableTreeNode node, JTree t,
	// boolean selected) {
	//
	// }

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		} catch (RecognitionException e) {
			log.warn(e.getMessage());
		} catch (NullPointerException e) {
			log.warn(e.getMessage()); // Null for adding a new mapping
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(apic.getPrefixManager());

		String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) // if no head
			query = queryHelper.getDefaultHead() + " " + OBDALibConstants.DATALOG_IMPLY_SYMBOL + " " + input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}
}
