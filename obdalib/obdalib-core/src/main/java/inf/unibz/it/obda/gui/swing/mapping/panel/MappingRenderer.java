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
package inf.unibz.it.obda.gui.swing.mapping.panel;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.IconLoader;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingBodyNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingHeadNode;
import inf.unibz.it.obda.gui.swing.mapping.tree.MappingNode;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences.MappingManagerPreferences;

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
import org.obda.query.domain.CQIE;
import org.obda.query.tools.parser.DatalogProgramParser;
import org.obda.query.tools.parser.DatalogQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long			serialVersionUID				= -4636107842424616156L;
	Icon								mappingIcon						= null;
	Icon								invalidmappingIcon				= null;
	Icon								mappingheadIcon					= null;
	Icon								mappingbodyIcon					= null;
	Icon								invalidmappingheadIcon			= null;

	final String						PATH_MAPPING_ICON				= "images/mapping.png";
	final String						PATH_INVALIDMAPPING_ICON		= "images/mapping_invalid.png";

	final String						PATH_MAPPINGHEAD_ICON			= "images/head.png";
	final String						PATH_INVALIDMAPPINGHEAD_ICON	= "images/head_invalid.png";
	final String						PATH_MAPPINGBODY_ICON			= "images/body.png";

	private JLabel						label							= null;
	private JTextPane					area							= null;
	private JPanel						panel							= null;
	private MappingManagerPreferences	pref							= null;
	private final APIController	apic;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public MappingRenderer(APIController apic, OBDAPreferences preference) {
		this.apic = apic;
		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		invalidmappingIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		invalidmappingheadIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);
		pref =  preference.getMappingsPreference();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

		return createComponent(node, tree, sel);
	}

	private JPanel createComponent(DefaultMutableTreeNode node, JTree t, boolean selected) {

		java.awt.GridBagConstraints grid;
		GridBagLayout l = new GridBagLayout();
		panel = new JPanel();
		panel.setLayout(l);
		label = new JLabel();
		area = new JTextPane();
		panel.setBackground(Color.white);
		label.setBackground(Color.white);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel ph = new JLabel();
		ph.setVisible(false);
		ph.setHorizontalAlignment(SwingConstants.LEADING);

		StyleContext context = new StyleContext();
		Style style = context.getStyle(StyleContext.DEFAULT_STYLE);

		if (node instanceof MappingNode) {
			if(!pref.getUseDefault()){
				StyleConstants.setFontFamily(style, pref.getFontFamily(MappingManagerPreferences.OBDAPREFS_FONTFAMILY));
				StyleConstants.setFontSize(style, pref.getFontSize(MappingManagerPreferences.OBDAPREFS_FONTSIZE));
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
			if(!pref.getUseDefault()){
				StyleConstants.setFontFamily(style, pref.getFontFamily(MappingManagerPreferences.OBDAPREFS_FONTFAMILY));
				StyleConstants.setFontSize(style, pref.getFontSize(MappingManagerPreferences.OBDAPREFS_FONTSIZE));
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

			MappingStyledDocument styledDoc = new MappingStyledDocument(context, apic, pref); // TODO change this to OBDAPreferences later
			try {
				styledDoc.insertString(0, txt, styledDoc.default_style);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			QueryPainter p = new QueryPainter(apic, pref); // TODO change this to OBDAPreferences later
			p.doRecoloring(styledDoc);
			while (p.isAlreadyColoring()) {
			}
			area.setDocument(styledDoc);

		}

		Color bg = new Color(220, 230, 240);

		if (selected) {

			panel.setBackground(bg);
			area.setBackground(bg);
			label.setBackground(bg);
		}

		grid = new java.awt.GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 0;
		grid.gridwidth = 1;
		grid.weightx = 0;
		grid.weighty = 0;
		grid.fill = java.awt.GridBagConstraints.VERTICAL;
		panel.add(label, grid, 0);

		grid = new java.awt.GridBagConstraints();
		grid.gridx = 2;
		grid.gridy = 0;
		grid.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		grid.fill = java.awt.GridBagConstraints.BOTH;
		grid.weightx = 1.0;
		grid.weighty = 1.0;
		panel.add(area, grid, 1);

		return panel;

	}

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		}
		catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		catch (NullPointerException e) {
			log.warn(e.getMessage()); // Null for adding a new mapping
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper =
			new DatalogQueryHelper(apic.getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1)  // if no head
			query = queryHelper.getDefaultHead() + " " +
			 	DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " +
			 	input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}
}
