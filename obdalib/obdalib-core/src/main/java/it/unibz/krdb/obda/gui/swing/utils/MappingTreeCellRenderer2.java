package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingBodyNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingHeadNode;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingNode;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import javax.swing.tree.TreeCellRenderer;

public class MappingTreeCellRenderer2 implements TreeCellRenderer {

	private JTextPane textPane;
	private JLabel iconLabel;
	private JPanel renderingComponent;

	private int preferredWidth;

	private int minTextHeight;

	private JTree componentBeingRendered;
	private boolean gettingCellBounds;

	private boolean renderIcon = true;

	private Font plainFont;

	private Font boldFont;

	public static final Color SELECTION_BACKGROUND = UIManager.getDefaults().getColor("List.selectionBackground");

	public static final Color SELECTION_FOREGROUND = UIManager.getDefaults().getColor("List.selectionForeground");

	public static final Color FOREGROUND = UIManager.getDefaults().getColor("List.foreground");

	final Icon mappingIcon;
	final Icon invalidmappingIcon;
	final Icon mappingheadIcon;
	final Icon mappingbodyIcon;
	final Icon invalidmappingheadIcon;

	final String PATH_MAPPING_ICON = "images/mapping.png";
	final String PATH_INVALIDMAPPING_ICON = "images/mapping_invalid.png";

	final String PATH_MAPPINGHEAD_ICON = "images/head.png";
	final String PATH_INVALIDMAPPINGHEAD_ICON = "images/head_invalid.png";
	final String PATH_MAPPINGBODY_ICON = "images/body.png";
	private int plainFontHeight;
	private OBDAPreferences preferences;
	private Style plainStyle;
	private Style boldStyle;
	private Style nonBoldStyle;
	private Style selectionForeground;
	private Style foreground;
	private Style fontSizeStyle;

	private int width;
	private int plainFontWidth;

	public MappingTreeCellRenderer2(OBDAPreferences preference) {

//		setPreferredWidth(width);
		this.preferences = preference;

		iconLabel = new JLabel("");
		iconLabel.setOpaque(false);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);

		textPane = new JTextPane();
		
		textPane.setOpaque(false);

		renderingComponent = new JPanel(new BorderLayout());
//		renderingComponent = new JPanel(new MappingTreeCellRendererLayoutManager());
		renderingComponent.add(iconLabel, BorderLayout.WEST);
		renderingComponent.add(textPane, BorderLayout.EAST);

		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		invalidmappingIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		invalidmappingheadIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);

		prepareStyles();
		setupFont();

	}

	private void prepareTextPane(Object value, boolean selected) {

		textPane.setBorder(null);
		String theVal = value.toString();
		textPane.setText(theVal);

		StyledDocument doc = textPane.getStyledDocument();
		resetStyles(doc);

		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

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
	}

	public int getPreferredWidth() {
		return preferredWidth;
	}

	public void setPreferredWidth(int preferredWidth) {
		this.preferredWidth = preferredWidth;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		componentBeingRendered = tree;
		// Rectangle cellBounds = new Rectangle();
		// if (!gettingCellBounds) {
		// gettingCellBounds = true;
		// cellBounds = tree.getRowBounds(row);
		// gettingCellBounds = false;
		// }
		preferredWidth = -1;
		minTextHeight = 12;
		tree.setToolTipText(value != null ? value.toString() : "");
		Component c = prepareRenderer(value, selected, hasFocus);
		return c;

	}

	private Component prepareRenderer(Object value, boolean isSelected, boolean hasFocus) {
//		String wrappedtext = wrap(value.toString());
		renderingComponent.setOpaque(isSelected);
		
//		renderingComponent.setPreferredSize(getRenderingDimensions(wrappedtext));
		prepareTextPane(value.toString(), isSelected);

		if (isSelected) {
			renderingComponent.setBackground(SELECTION_BACKGROUND);
			textPane.setForeground(SELECTION_FOREGROUND);
		} else {
			renderingComponent.setBackground(componentBeingRendered.getBackground());
			textPane.setForeground(componentBeingRendered.getForeground());
		}

		final Icon icon = getIcon(value);
		iconLabel.setIcon(icon);
		if (icon != null) {
			iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), plainFontHeight));
		}
		renderingComponent.revalidate();
		return renderingComponent;
	}

	private String wrap(String text) {
		StringBuffer wrappedText = new StringBuffer();

		Dimension treeSize = componentBeingRendered.getSize();
		int width = treeSize.width > 0 ? treeSize.width : 700;

		int maxChars = width / (plainFontWidth + 2);
		StringTokenizer tokenizer = new StringTokenizer(text);
		int currentLineSize = 0;
		while (tokenizer.hasMoreTokens()) {
			String newText = tokenizer.nextToken();
			if (currentLineSize + 1 + newText.length() > maxChars) {
				wrappedText.append("\n");
				wrappedText.append(newText);
				currentLineSize = 0;
			} else {
				if (currentLineSize != 0)
					wrappedText.append(" ");
				wrappedText.append(newText);
				currentLineSize += (1 + newText.length());
			}
		}
		return wrappedText.toString();

	}

	private Dimension getRenderingDimensions(String content) {
		Dimension treeSize = componentBeingRendered.getSize();
		Dimension box = null;
		int width = treeSize.width > 0 ? treeSize.width : 800;
		String[] lines = content.split("\n");
		int height = (plainFontHeight * lines.length) + (2 * lines.length);
		box = new Dimension(width, height);
		return box;

	}

	private int getFontSize() {
		return Integer.parseInt(preferences.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());
	}

	private void setupFont() {

		// StyleConstants.setFontFamily(style, );
		// StyleConstants.setFontSize(style,
		// Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));

		plainFont = new Font("Dialog", Font.PLAIN, 14);
		// plainFont =
		// Font.getFont(preferences.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString().trim());
		plainFontHeight = iconLabel.getFontMetrics(plainFont).getHeight();
		int[] widths = iconLabel.getFontMetrics(plainFont).getWidths();
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < widths.length; i++) {
			int j = widths[i];
			if (j > max)
				max = j;

		}
		plainFontWidth = max;
		boldFont = plainFont.deriveFont(Font.BOLD);
		textPane.setFont(plainFont);
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
			Insets rcInsets = renderingComponent.getInsets();

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
			Insets rcInsets = renderingComponent.getInsets();

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
