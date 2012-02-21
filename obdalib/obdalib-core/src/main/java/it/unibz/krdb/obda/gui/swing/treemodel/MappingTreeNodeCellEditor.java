/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.parser.DatalogProgramParser;
import it.unibz.krdb.obda.parser.DatalogQueryHelper;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingTreeNodeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4107989676987230404L;
	private OBDAModel controller = null;
	// private MappingEditorPanel parent = null;
	private TargetQueryVocabularyValidator validator = null;

	private int preferredWidth;

	private int minTextHeight;

	private boolean bEditingCanceled = false;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	final String PATH_MAPPING_ICON = "images/mapping.png";
	final String PATH_INVALIDMAPPING_ICON = "images/mapping_invalid.png";

	final String PATH_MAPPINGHEAD_ICON = "images/head.png";
	final String PATH_INVALIDMAPPINGHEAD_ICON = "images/head_invalid.png";
	final String PATH_MAPPINGBODY_ICON = "images/body.png";

	private DefaultMutableTreeNode node = null;
	private Font plainFont;
	private int plainFontHeight;
	private Font boldFont;
	private OBDAPreferences preferences;
	private JLabel iconLabel;
	private JTextPane textPane;
	private JPanel editingComponent;
	private Icon mappingIcon;
	private Icon invalidmappingIcon;
	private Icon mappingheadIcon;
	private Icon invalidmappingheadIcon;
	private Icon mappingbodyIcon;
	private Style plainStyle;
	private Style boldStyle;
	private Style nonBoldStyle;
	private Style selectionForeground;
	private Style foreground;
	private Style fontSizeStyle;
	private JTree componentBeingRendered;
	private boolean renderIcon = true;

	public static final Color SELECTION_BACKGROUND = UIManager.getDefaults().getColor("List.selectionBackground");

	public static final Color SELECTION_FOREGROUND = UIManager.getDefaults().getColor("List.selectionForeground");

	public static final Color FOREGROUND = UIManager.getDefaults().getColor("List.foreground");

	public static final Color INVALID = Color.RED;

	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();

	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();

	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;

	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;

	boolean invalid = false;
	private Style invalidQuery;
	private Border defaultBorder;
	private Border outerBorder;
	private Border stateBorder;
	private Border errorBorder;
	private Stroke errorStroke;
	private DocumentListener docListener;
	private Timer timer;
	private int errorStartIndex;
	private int errorEndIndex;
	private RecognitionException parsingException;

	public MappingTreeNodeCellEditor(OBDAModel apic, TargetQueryVocabularyValidator validator, OBDAPreferences preference) {

		controller = apic;
		this.validator = validator;

		this.preferences = preference;

		iconLabel = new JLabel("");
		iconLabel.setOpaque(false);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);

		textPane = new JTextPane();
		textPane.setOpaque(false);

		editingComponent = new JPanel(new MappingTreeCellRendererLayoutManager());
		editingComponent.add(iconLabel);
		editingComponent.add(textPane);

		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		invalidmappingIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		invalidmappingheadIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);

		textPane.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
					stopCellEditing();
					// mappingmanagerpanel.stopTreeEditing();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// bEditingCanceled = true;
					cancelCellEditing();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Does nothing
			}

			@Override
			public void keyTyped(KeyEvent e) {
				// Does nothing
			}
		});

		prepareStyles();
		setupFont();

		this.outerBorder = null;
		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		setStateBorder(defaultBorder);
		errorBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.RED);
		errorStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 3.0f, new float[] { 4.0f, 2.0f }, 0.0f);

		docListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				handleDocumentUpdated();
			}

			public void removeUpdate(DocumentEvent e) {
				handleDocumentUpdated();
			}

			public void changedUpdate(DocumentEvent e) {
			}
		};
		textPane.getStyledDocument().addDocumentListener(docListener);

		timer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleTimer();
			}
		});

	}

	private void handleDocumentUpdated() {
		timer.restart();
		clearError();
		// performHighlighting();
	}

	private void handleTimer() {
		timer.stop();
		checkExpression();
	}

	public void setStateBorder(Border border) {
		stateBorder = border;
		this.editingComponent.setBorder(BorderFactory.createCompoundBorder(outerBorder, stateBorder));
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {

		componentBeingRendered = tree;
		preferredWidth = -1;
		minTextHeight = 12;
		node = (DefaultMutableTreeNode) value;
		tree.setToolTipText(value != null ? value.toString() : "");
		Component c = prepareRenderer(value, isSelected);
		return c;

	}

	private Component prepareRenderer(Object value, boolean isSelected) {
		editingComponent.setOpaque(isSelected);
		editingComponent.setBorder(new EtchedBorder());

		prepareTextPane(value.toString(), isSelected);

		if (isSelected) {
			editingComponent.setBackground(SELECTION_BACKGROUND);
			textPane.setForeground(SELECTION_FOREGROUND);
		} else {
			editingComponent.setBackground(componentBeingRendered.getBackground());
			textPane.setForeground(componentBeingRendered.getForeground());
		}

		final Icon icon = getIcon(value);
		iconLabel.setIcon(icon);
		if (icon != null) {
			iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), plainFontHeight));
		}
		editingComponent.revalidate();
		return editingComponent;
	}

	private void prepareTextPane(Object value, boolean selected) {

		textPane.setBorder(null);
		String theVal = value.toString();
		textPane.setText(theVal);

		StyledDocument doc = textPane.getStyledDocument();
		resetStyles(doc);
//
//		if (invalid) {
//			doc.setParagraphAttributes(0, doc.getLength(), invalidQuery, false);
//		} else 
			if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

	}

	private int getFontSize() {
		return Integer.parseInt(preferences.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());
		// return 14;
	}

	private void setupFont() {

		// StyleConstants.setFontFamily(style, );
		// StyleConstants.setFontSize(style,
		// Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));

		plainFont = new Font("Dialog", Font.PLAIN, 14);
		// plainFont =
		// Font.getFont(preferences.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString().trim());
		plainFontHeight = iconLabel.getFontMetrics(plainFont).getHeight();
		boldFont = plainFont.deriveFont(Font.BOLD);
		textPane.setFont(plainFont);
	}

	private void resetStyles(StyledDocument doc) {
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
		StyleConstants.setFontSize(fontSizeStyle, getFontSize());
		Font f = plainFont;
		StyleConstants.setFontFamily(fontSizeStyle, f.getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, false);
		setupFont();
	}

	private void prepareStyles() {
		StyledDocument doc = textPane.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		// StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		// StyleConstants.setFontFamily(plainStyle,
		// textPane.getFont().getFamily());

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		selectionForeground = doc.addStyle("SEL_FG_STYPE", null);
		// we know that it is possible for SELECTION_FOREGROUND to be null
		// and an exception here means that Protege doesn't start
		if (selectionForeground != null && SELECTION_FOREGROUND != null) {
			StyleConstants.setForeground(selectionForeground, SELECTION_FOREGROUND);
		}

		foreground = doc.addStyle("FG_STYLE", null);
		if (foreground != null && FOREGROUND != null) {
			StyleConstants.setForeground(foreground, FOREGROUND);
		}

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		invalidQuery = doc.addStyle("INVALID_STYLE", null);
		StyleConstants.setForeground(invalidQuery, INVALID);
	}

	private void validate() throws Exception {
		final String text = textPane.getText().trim();
		// final Object node = parent.getEditingObject();

		if (text.isEmpty()) {
			String msg = String.format("ERROR: The %s cannot be empty!", node.toString().toLowerCase());
			throw new Exception(msg);
		}

		if (node instanceof MappingHeadNode) {
			final CQIE query = parse(text);
			if (query == null) {
				throw new Exception("Syntax error.");
			}
			if (!validator.validate(query)) {
				Vector<String> invalidPredicates = validator.getInvalidPredicates();
				String invalidList = "";
				for (String predicate : invalidPredicates) {
					invalidList += "- " + predicate + "\n";
				}
				String msg = String.format("ERROR: The below list of predicates is unknown by the ontology: \n %s", invalidList);
				invalid = true;
				throw new Exception(msg);
			}
		}
		invalid = false;
	}

	protected void checkExpression() {
		try {
			// Parse the text in the editor. If no parse
			// exception is thrown, clear the error, otherwise
			// set the error
			validate();
			setError(null);
		} catch (Exception e) {
			setError(e);
		}
	}

	private void setError(Exception e) {

		if (e != null) {
			ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
			ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
			textPane.setToolTipText(getHTMLErrorMessage(e.getMessage()));
			setStateBorder(errorBorder);
			// setErrorRange(e.getStartIndex(), e.getEndIndex());
		} else {
			clearError();
		}
	}

	private void clearError() {
		textPane.setToolTipText(null);
		setStateBorder(defaultBorder);
		// setErrorRange(0, 0);
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}

	private static String getHTMLErrorMessage(String msg) {
		String html = "<html><body>";
		msg = msg.replace("\n", "<br>");
		msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		html += msg;
		html += "</body></html>";
		return html;
	}

	// private void setErrorRange(int startIndex, int endIndex) {
	// errorStartIndex = startIndex;
	// errorEndIndex = endIndex;
	// repaint();
	// }

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		} catch (RecognitionException e) {
			this.parsingException = e;
			log.warn(e.getMessage(), e);
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper = new DatalogQueryHelper(controller.getPrefixManager());

		String[] atoms = input.split(OBDALibConstants.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1) { // if no head
			query = String.format("%s %s %s", queryHelper.getDefaultHead(), OBDALibConstants.DATALOG_IMPLY_SYMBOL, input);
		}
		query = queryHelper.getPrefixes() + query; // Append the prefixes

		return query;
	}

	// @Override
	// public void addCellEditorListener(CellEditorListener arg0) {
	// listener.add(arg0);
	// }

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	@Override
	public Object getCellEditorValue() {
		return textPane.getText();
	}

	@Override
	public boolean stopCellEditing() {
		try {
			validate();
			setError(null);
		} catch (Exception e) {
			setError(e);			
			return false;
		}
		fireEditingStopped();

		return true;
	}

	protected Icon getIcon(Object object) {
		if (!renderIcon) {
			return null;
		}
		if (object instanceof MappingBodyNode) {
			return this.mappingbodyIcon;
		} else if (object instanceof MappingHeadNode) {
			return this.mappingheadIcon;
		} else if (object instanceof MappingNode) {
			return this.mappingIcon;
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// A custom layout manager for speed
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	private class MappingTreeCellRendererLayoutManager implements LayoutManager2 {

		/***
		 * From Protege 4's OWLCellRendererLayoutManager.java
		 */

		/**
		 * Adds the specified component to the layout, using the specified
		 * constraint object.
		 * 
		 * @param comp
		 *            the component to be added
		 * @param constraints
		 *            where/how the component is added to the layout.
		 */
		public void addLayoutComponent(Component comp, Object constraints) {
			// We only have three components the label that holds the icon
			// the text area
		}

		/**
		 * Calculates the maximum size dimensions for the specified container,
		 * given the components it contains.
		 * 
		 * @see java.awt.Component#getMaximumSize
		 * @see java.awt.LayoutManager
		 */
		public Dimension maximumLayoutSize(Container target) {
			return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		/**
		 * Returns the alignment along the x axis. This specifies how the
		 * component would like to be aligned relative to other components. The
		 * value should be a number between 0 and 1 where 0 represents alignment
		 * along the origin, 1 is aligned the furthest away from the origin, 0.5
		 * is centered, etc.
		 */
		public float getLayoutAlignmentX(Container target) {
			return 0;
		}

		/**
		 * Returns the alignment along the y axis. This specifies how the
		 * component would like to be aligned relative to other components. The
		 * value should be a number between 0 and 1 where 0 represents alignment
		 * along the origin, 1 is aligned the furthest away from the origin, 0.5
		 * is centered, etc.
		 */
		public float getLayoutAlignmentY(Container target) {
			return 0;
		}

		/**
		 * Invalidates the layout, indicating that if the layout manager has
		 * cached information it should be discarded.
		 */
		public void invalidateLayout(Container target) {
		}

		/**
		 * If the layout manager uses a per-component string, adds the component
		 * <code>comp</code> to the layout, associating it with the string
		 * specified by <code>name</code>.
		 * 
		 * @param name
		 *            the string to be associated with the component
		 * @param comp
		 *            the component to be added
		 */
		public void addLayoutComponent(String name, Component comp) {
		}

		/**
		 * Removes the specified component from the layout.
		 * 
		 * @param comp
		 *            the component to be removed
		 */
		public void removeLayoutComponent(Component comp) {
		}

		/**
		 * Calculates the preferred size dimensions for the specified container,
		 * given the components it contains.
		 * 
		 * @param parent
		 *            the container to be laid out
		 * @see #minimumLayoutSize
		 */
		public Dimension preferredLayoutSize(Container parent) {

			int iconWidth;
			int iconHeight;
			int textWidth;
			int textHeight;
			int width;
			int height;
			iconWidth = iconLabel.getPreferredSize().width;
			iconHeight = iconLabel.getPreferredSize().height;
			Insets insets = parent.getInsets();
			Insets rcInsets = editingComponent.getInsets();

			if (preferredWidth != -1) {
				textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
				View v = textPane.getUI().getRootView(textPane);
				v.setSize(textWidth, Integer.MAX_VALUE);
				textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
				width = preferredWidth;
			} else {
				textWidth = textPane.getPreferredSize().width;
				textHeight = textPane.getPreferredSize().height;
				width = textWidth + iconWidth;
			}
			if (textHeight < iconHeight) {
				height = iconHeight;
			} else {
				height = textHeight;
			}
			int minHeight = minTextHeight;
			if (height < minHeight) {
				height = minHeight;
			}
			int totalWidth = width + rcInsets.left + rcInsets.right;
			int totalHeight = height + rcInsets.top + rcInsets.bottom;
			return new Dimension(totalWidth, totalHeight);
		}

		/**
		 * Lays out the specified container.
		 * 
		 * @param parent
		 *            the container to be laid out
		 */
		public void layoutContainer(Container parent) {
			int iconWidth;
			int iconHeight;
			int textWidth;
			int textHeight;
			int deprecatedWidth;
			int deprecatedHeight;
			Insets rcInsets = editingComponent.getInsets();

			iconWidth = iconLabel.getPreferredSize().width;
			iconHeight = iconLabel.getPreferredSize().height;
			if (preferredWidth != -1) {
				textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right;
				View v = textPane.getUI().getRootView(textPane);
				v.setSize(textWidth, Integer.MAX_VALUE);
				textHeight = (int) v.getMinimumSpan(View.Y_AXIS);
			} else {
				textWidth = textPane.getPreferredSize().width;
				textHeight = textPane.getPreferredSize().height;
				if (textHeight < minTextHeight) {
					textHeight = minTextHeight;
				}
			}
			int leftOffset = rcInsets.left;
			int topOffset = rcInsets.top;
			iconLabel.setBounds(leftOffset, topOffset, iconWidth, iconHeight);
			textPane.setBounds(leftOffset + iconWidth, topOffset, textWidth, textHeight);
		}

		/**
		 * Calculates the minimum size dimensions for the specified container,
		 * given the components it contains.
		 * 
		 * @param parent
		 *            the component to be laid out
		 * @see #preferredLayoutSize
		 */
		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

	}

}
