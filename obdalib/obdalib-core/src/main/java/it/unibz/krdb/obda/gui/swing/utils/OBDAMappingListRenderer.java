package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.codec.SourceQueryToTextCodec;
import it.unibz.krdb.obda.codec.TargetQeryToTextCodec;
import it.unibz.krdb.obda.gui.swing.IconLoader;
import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

public class OBDAMappingListRenderer implements ListCellRenderer {

	private JTextPane mapTextPane;
	private JLabel mapIconLabel;

	private JTextPane trgQueryTextPane;
	private JTextPane srcQueryTextPane;

	private JLabel trgQueryIconLabel;
	private JLabel srcQueryIconLabel;

	private JPanel renderingComponent;

	private int preferredWidth;

	private int minTextHeight;

	private JList componentBeingRendered;
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

	SourceQueryToTextCodec srccodec;
	TargetQeryToTextCodec trgcodec;
	private Style background;
	private Style alignment;
	private JPanel trgQueryPanel;
	private JPanel srcQueryPanel;
	private JPanel mapPanel;
	private int heightTargetQuery;
	private int heightSourceQuery;
	private int heightID;
	private JPanel mainPanel;
	private QueryPainter painter;

	public OBDAMappingListRenderer(OBDAPreferences preference, OBDAModel apic, TargetQueryVocabularyValidator validator) {

		// setPreferredWidth(width);
		this.preferences = preference;

		srccodec = new SourceQueryToTextCodec(apic);
		trgcodec = new TargetQeryToTextCodec(apic);

		trgQueryIconLabel = new JLabel("");
		trgQueryIconLabel.setVerticalAlignment(SwingConstants.TOP);

		srcQueryIconLabel = new JLabel("");
		srcQueryIconLabel.setVerticalAlignment(SwingConstants.TOP);

		mapIconLabel = new JLabel("");
		mapIconLabel.setVerticalAlignment(SwingConstants.TOP);

		trgQueryTextPane = new JTextPane();
		painter = new QueryPainter(apic, preference, trgQueryTextPane, validator);

		trgQueryTextPane.setMargin(new Insets(4, 4, 4, 4));

		srcQueryTextPane = new JTextPane();
		srcQueryTextPane.setMargin(new Insets(4, 4, 4, 4));

		mapTextPane = new JTextPane();
		mapTextPane.setMargin(new Insets(4, 4, 4, 4));

		trgQueryPanel = new JPanel();
		trgQueryPanel.add(trgQueryIconLabel);
		trgQueryPanel.add(trgQueryTextPane);

		srcQueryPanel = new JPanel();
		srcQueryPanel.add(srcQueryIconLabel);
		srcQueryPanel.add(srcQueryTextPane);

		mapPanel = new JPanel();
		mapPanel.add(mapIconLabel);
		mapPanel.add(mapTextPane);

		mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		// mainPanel.add(this.mapIconLabel);
		mainPanel.add(this.mapTextPane);
		mainPanel.add(this.trgQueryIconLabel);
		mainPanel.add(this.trgQueryTextPane);
		mainPanel.add(this.srcQueryIconLabel);
		mainPanel.add(srcQueryTextPane);

		mainPanel.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192), 1));
		// srcQueryTextPane.setBorder(BorderFactory.createLineBorder(new
		// Color(192,192,192), 1));
		// trgQueryTextPane.setBorder(BorderFactory.createLineBorder(new
		// Color(192,192,192), 1));

		mainPanel.setOpaque(true);
		mapTextPane.setOpaque(false);
		srcQueryTextPane.setOpaque(false);
		trgQueryTextPane.setOpaque(false);
		mapIconLabel.setOpaque(false);
		srcQueryIconLabel.setOpaque(false);
		trgQueryIconLabel.setOpaque(false);

		mappingIcon = IconLoader.getImageIcon(PATH_MAPPING_ICON);
		invalidmappingIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPING_ICON);
		mappingheadIcon = IconLoader.getImageIcon(PATH_MAPPINGHEAD_ICON);
		invalidmappingheadIcon = IconLoader.getImageIcon(PATH_INVALIDMAPPINGHEAD_ICON);
		mappingbodyIcon = IconLoader.getImageIcon(PATH_MAPPINGBODY_ICON);

		// renderingComponent = mainPanel;

		SpringLayout layout = new SpringLayout();

		renderingComponent = new JPanel(layout);
		renderingComponent.setBorder(null);
		renderingComponent.add(mainPanel);

		layout.putConstraint(SpringLayout.WEST, mainPanel, 2, SpringLayout.WEST, renderingComponent);
		layout.putConstraint(SpringLayout.NORTH, mainPanel, 2, SpringLayout.NORTH, renderingComponent);

		renderingComponent.setBackground(Color.white);

		prepareStyles();
		setupFont();

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
		StyledDocument doc = trgQueryTextPane.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		// StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
//		StyleConstants.setSpaceAbove(plainStyle, 0);
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

		background = doc.addStyle("BG_STYLE", null);
		if (background != null) {
			// StyleConstants.setBackground(background, Color.WHITE);
		}

		alignment = doc.addStyle("ALIGNMENT_STYLE", null);
		if (alignment != null) {
			StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_LEFT);
		}

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

	}

	// public int getPreferredWidth() {
	// return preferredWidth;
	// }

	public void setPreferredWidth(int preferredWidth) {
		// System.out.println("Called prefered width!!!!");
		this.preferredWidth = preferredWidth;
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
		plainFontHeight = trgQueryIconLabel.getFontMetrics(plainFont).getHeight();

		int[] widths = trgQueryIconLabel.getFontMetrics(plainFont).getWidths();
		int sum = 0;
		for (int i = 0; i < widths.length; i++) {
			int j = widths[i];
			sum += j;
		}
		plainFontWidth = sum / widths.length;
		boldFont = plainFont.deriveFont(Font.BOLD);
		trgQueryTextPane.setFont(plainFont);
	}

	int iconWidth;
	int iconHeight;
	int textWidth;
	int textidHeight;
	int textTargetHeight;
	int textSourceHeight;
	// int width;
	int height;
	int totalWidth;
	int totalHeight;

	private void computeDimensions(JPanel parent) {
		iconWidth = trgQueryIconLabel.getPreferredSize().width;
		iconHeight = trgQueryIconLabel.getPreferredSize().height;
		Insets insets = parent.getInsets();
		Insets rcInsets = renderingComponent.getInsets();

		// Insets rcInsets = mainPanel.getInsets();

		if (preferredWidth != -1) {

			textWidth = preferredWidth - iconWidth - rcInsets.left - rcInsets.right - 6;

			int maxChars = (textWidth / (plainFontWidth)) - 10;

			String trgQuery = trgQueryTextPane.getText();
			int linesTarget = (int) (trgQuery.length() / maxChars) + 1;
			String srcQuery = srcQueryTextPane.getText();
			int linesSource = (int) (srcQuery.length() / maxChars) + 1;

			textTargetHeight = minTextHeight * linesTarget; // target
			textSourceHeight = minTextHeight * linesSource; // source
			textidHeight = minTextHeight;

			View v = trgQueryTextPane.getUI().getRootView(trgQueryTextPane);
			// v.setSize(textWidth, Integer.MAX_VALUE);
			// v.setSize(textWidth, 0);
			trgQueryTextPane.setPreferredSize(new Dimension(textWidth, textTargetHeight));

			v = srcQueryTextPane.getUI().getRootView(srcQueryTextPane);
			// v.setSize(textWidth, Integer.MAX_VALUE);
			// v.setSize(textWidth, 0);
			srcQueryTextPane.setPreferredSize(new Dimension(textWidth, textSourceHeight));

			v = mapTextPane.getUI().getRootView(mapTextPane);
			// v.setSize(textWidth, Integer.MAX_VALUE);
			// v.setSize(textWidth, 0);
			mapTextPane.setPreferredSize(new Dimension(textWidth, textidHeight));

			// textidHeight = (int) v.getMinimumSpan(View.Y_AXIS);
			width = preferredWidth;
		} else {

			/***
			 * This block is not used
			 */
			textWidth = mapTextPane.getPreferredSize().width;

			textidHeight = mapTextPane.getPreferredSize().height;
			textSourceHeight = srcQueryTextPane.getPreferredSize().height;
			textTargetHeight = trgQueryTextPane.getPreferredSize().width;

			width = textWidth + iconWidth;
		}
		if (textidHeight < iconHeight) {
			height = iconHeight;
		} else {
			height = textidHeight;
		}
		int minHeight = minTextHeight;
		if (height < minHeight) {
			height = minHeight;
		}
		// totalWidth = width + rcInsets.left + rcInsets.right;
		totalWidth = width;
		// totalHeight = textidHeight + textSourceHeight + textTargetHeight +
		// rcInsets.top + rcInsets.bottom;
		totalHeight = textidHeight + textSourceHeight + textTargetHeight;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

		componentBeingRendered = list;
		// Rectangle cellBounds = new Rectangle();
		// if (!gettingCellBounds) {
		// gettingCellBounds = true;
		// cellBounds = tree.getRowBounds(row);
		// gettingCellBounds = false;
		// }
		if (list.getParent() != null) {
			preferredWidth = list.getParent().getWidth();
		}

		minTextHeight = this.plainFontHeight + 6;
		Component c = prepareRenderer((OBDAMappingAxiom) value, isSelected, cellHasFocus);
		// c.invalidate();
		return c;
	}

	private Component prepareRenderer(OBDAMappingAxiom value, boolean isSelected, boolean hasFocus) {
		// String wrappedtext = wrap(value.toString());
		renderingComponent.setOpaque(false);

		// renderingComponent.setPreferredSize(getRenderingDimensions(wrappedtext));
		prepareTextPanes(value, isSelected);
		computeTextPaneHight(value);

		if (isSelected) {
			// renderingComponent.setBackground(Color.white);

			mainPanel.setBackground(SELECTION_BACKGROUND);
		} else {
			mainPanel.setBackground(new Color(240, 245, 240));
		}

		computeDimensions(renderingComponent);

		trgQueryIconLabel.setPreferredSize(new Dimension(this.mappingheadIcon.getIconWidth(), textTargetHeight));
		srcQueryIconLabel.setPreferredSize(new Dimension(this.mappingbodyIcon.getIconWidth(), textSourceHeight));
		mapIconLabel.setIcon(this.mappingIcon);

		trgQueryTextPane.setPreferredSize(new Dimension(textWidth, textTargetHeight));
		srcQueryTextPane.setPreferredSize(new Dimension(textWidth, textSourceHeight));
		mapTextPane.setPreferredSize(new Dimension(textWidth + iconWidth, textidHeight));

		mainPanel.setPreferredSize(new Dimension(totalWidth - 4, this.totalHeight + 2));
		renderingComponent.setPreferredSize(new Dimension(totalWidth, this.totalHeight + 5));

		boolean debugColors = false;

		if (debugColors) {

			mapTextPane.setOpaque(true);
			srcQueryTextPane.setOpaque(true);
			trgQueryTextPane.setOpaque(true);
			mapIconLabel.setOpaque(true);
			srcQueryIconLabel.setOpaque(true);
			trgQueryIconLabel.setOpaque(true);

			mainPanel.setBackground(Color.yellow);
			renderingComponent.setBackground(Color.yellow);
			trgQueryTextPane.setBackground(Color.ORANGE);
			trgQueryTextPane.setOpaque(true);
			trgQueryIconLabel.setBackground(Color.red);
			trgQueryPanel.setBackground(Color.orange);

			srcQueryTextPane.setBackground(Color.pink);
			srcQueryIconLabel.setBackground(Color.cyan);
			srcQueryPanel.setBackground(Color.yellow);

			mapTextPane.setBackground(Color.green);
			mapIconLabel.setBackground(Color.magenta);
			mapPanel.setBackground(Color.BLACK);
			mapIconLabel.setBackground(Color.black);
			srcQueryIconLabel.setBackground(Color.blue);
			trgQueryIconLabel.setBackground(Color.red);
		}
		try {
			painter.recolorQuery();
		} catch (Exception e) {
//			System.out.println("Error");
		}
		trgQueryTextPane.setBorder(null);

		renderingComponent.revalidate();
		return renderingComponent;
	}

	private void computeTextPaneHight(OBDAMappingAxiom value) {

	}

	private void prepareTextPanes(OBDAMappingAxiom value, boolean selected) {

		String trgQuery = trgcodec.encode(value.getTargetQuery());
		trgQueryTextPane.setText(trgQuery);
		String srcQuery = srccodec.encode(value.getSourceQuery());
		srcQueryTextPane.setText(srcQuery);
		mapTextPane.setText(value.getId());

		StyledDocument doc = trgQueryTextPane.getStyledDocument();
		resetStyles(doc);
		doc.setParagraphAttributes(0, doc.getLength(), background, false);
		doc.setParagraphAttributes(0, doc.getLength(), alignment, false);
		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

		doc = srcQueryTextPane.getStyledDocument();
		resetStyles(doc);
		doc.setParagraphAttributes(0, doc.getLength(), background, false);
		doc.setParagraphAttributes(0, doc.getLength(), alignment, false);
		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

		doc = mapTextPane.getStyledDocument();
		resetStyles(doc);
		doc.setParagraphAttributes(0, doc.getLength(), boldStyle, false);
		doc.setParagraphAttributes(0, doc.getLength(), background, false);
		doc.setParagraphAttributes(0, doc.getLength(), alignment, false);
		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground, false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

	}

}
