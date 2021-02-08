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

import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.protege.core.OBDAModel;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class MappingListRenderer implements ListCellRenderer<SQLPPTriplesMap> {

	private final OBDAModel obdaModel;

	private final JTextPane mapTextPane;
	private final JTextPane trgQueryTextPane;
	private final JTextPane srcQueryTextPane;
	private final JPanel mainPanel;
	private final JPanel renderingComponent;

	private final Font plainFont;
	private final int plainFontHeight;
	private final int plainFontWidth;

	private static final Color SELECTION_BACKGROUND_COLOR = UIManager.getDefaults().getColor("List.selectionBackground");
	private static final Color BACKGROUND_COLOR = new Color(240, 245, 240);

	private final SimpleAttributeSet mappingIdStyle = new SimpleAttributeSet();
	private final SimpleAttributeSet selectionForeground = new SimpleAttributeSet();
	private final SimpleAttributeSet foreground = new SimpleAttributeSet();

	private static final int PADDING = 2;
	private static final int MARGIN = 10;
	private static final int BORDER = 1;
	private static final int SEPARATOR = 2;

	public MappingListRenderer(OBDAModelManager obdaModelManager) {
		obdaModel = obdaModelManager.getActiveOBDAModel();

		mapTextPane = new JTextPane();
		mapTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		mapTextPane.setOpaque(false);

		trgQueryTextPane = new JTextPane();
		trgQueryTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		trgQueryTextPane.setOpaque(false);
		trgQueryTextPane.setDocument(new TargetQueryStyledDocument(
				obdaModelManager,
				doc -> {
					try {
						doc.validate();
					}
					catch (TargetQueryParserException ignore) {
					}
				}));

		srcQueryTextPane = new JTextPane();
		srcQueryTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		srcQueryTextPane.setOpaque(false);
		srcQueryTextPane.setDocument(new SQLQueryStyledDocument());

		mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mainPanel.add(mapTextPane);
		mainPanel.add(trgQueryTextPane);
		mainPanel.add(srcQueryTextPane);
		mainPanel.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192), BORDER));
		mainPanel.setOpaque(true);

		SpringLayout layout = new SpringLayout();
		renderingComponent = new JPanel(layout);
		renderingComponent.add(mainPanel);
		layout.putConstraint(SpringLayout.WEST, mainPanel, PADDING, SpringLayout.WEST, renderingComponent);
		layout.putConstraint(SpringLayout.NORTH, mainPanel, PADDING, SpringLayout.NORTH, renderingComponent);

		renderingComponent.setOpaque(false);

		plainFont = TargetQueryStyledDocument.TARGET_QUERY_FONT;
		plainFontHeight = trgQueryTextPane.getFontMetrics(plainFont).getHeight();
		int[] widths = trgQueryTextPane.getFontMetrics(plainFont).getWidths();
		int sum = 0;
		for (int j : widths)
			sum += j;
		plainFontWidth = sum / widths.length;

		StyleConstants.setFontFamily(mappingIdStyle, plainFont.getFamily());
		StyleConstants.setFontSize(mappingIdStyle, plainFont.getSize());
		StyleConstants.setBold(mappingIdStyle, true);

		Color selectionForegroundColor = UIManager.getDefaults().getColor("List.selectionForeground");
		if (selectionForegroundColor != null)
			StyleConstants.setForeground(selectionForeground, selectionForegroundColor);

		Color foregroundColor = UIManager.getDefaults().getColor("List.foreground");
		if (foregroundColor != null)
			StyleConstants.setForeground(foreground, foregroundColor);
	}


	private void setAttibutes(JTextPane pane, SimpleAttributeSet attibutes, boolean replace) {
		StyledDocument doc = pane.getStyledDocument();
		doc.setParagraphAttributes(0, doc.getLength(), attibutes, replace);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends SQLPPTriplesMap> list, SQLPPTriplesMap value, int index, boolean isSelected, boolean cellHasFocus) {

		trgQueryTextPane.setText(obdaModel.getTargetRendering(value));
		setAttibutes(trgQueryTextPane, isSelected ? selectionForeground : foreground,false);

		srcQueryTextPane.setText(value.getSourceQuery().getSQL());
		setAttibutes(srcQueryTextPane, isSelected ? selectionForeground : foreground,false);

		mapTextPane.setText(value.getId());
		setAttibutes(mapTextPane, mappingIdStyle, true);
		setAttibutes(mapTextPane, isSelected ? selectionForeground : foreground,false);

		mainPanel.setBackground(isSelected ? SELECTION_BACKGROUND_COLOR : BACKGROUND_COLOR);

		int preferredWidth = list.getParent().getWidth();
		if (preferredWidth == 0) { // JViewport returns 0 when not visible (?)
			preferredWidth = list.getParent().getParent().getWidth();
		}

		/*
		 * Now we compute the sizes of each of the components, including the text
		 * panes. Note that for the target text pane we need to compute the number
		 * of lines that the text will require, and the height of these lines
		 * accordingly. Right now this is done in an approximate way using
		 * FontMetrics. It works fine most of the time, however, the algorithm needs
		 * to be improved. For the source query we have an even cruder
		 * implementation that needs to be adjusted in the same way as the one for
		 * the target query (to compute the number of lines using FontMetrics instead
		 * of "maxChars".
		 */
		int mainPanelWidth = preferredWidth - 2 * BORDER - 2 * PADDING;
		int textWidth = mainPanelWidth - 2 * MARGIN;
		int minTextHeight = plainFontHeight + 2 * SEPARATOR;

		mapTextPane.setPreferredSize(new Dimension(textWidth, minTextHeight));

		int textTargetHeight = minTextHeight * getTargetQueryLinesNumber(textWidth);
		trgQueryTextPane.setPreferredSize(new Dimension(textWidth, textTargetHeight));

		int maxChars = (textWidth / plainFontWidth) - 10;
		int linesSource = srcQueryTextPane.getText().length() / maxChars + 1;
		int textSourceHeight = minTextHeight * linesSource;
		srcQueryTextPane.setPreferredSize(new Dimension(textWidth, textSourceHeight));

		int totalHeight = minTextHeight + textSourceHeight + textTargetHeight + 2 * BORDER;
		mainPanel.setPreferredSize(new Dimension(mainPanelWidth, totalHeight));
		renderingComponent.setPreferredSize(new Dimension(preferredWidth, totalHeight + 2 * PADDING));

		renderingComponent.revalidate();
		return renderingComponent;
	}

	/*
	 * Computing the number of lines for the target query base on the
	 * FontMetrics. We are going to simulate a "wrap" operation over the
	 * text of the target query in order to be able to count number of
	 * lines.
	 */
	private int getTargetQueryLinesNumber(int textWidth) {
		String[] split = trgQueryTextPane.getText().split(" ");
		int currentWidth = 0;
		StringBuilder currentLine = new StringBuilder();
		int linesTarget = 1;
		FontMetrics m = trgQueryTextPane.getFontMetrics(plainFont);
		for (String splitst : split) {
			boolean space = false;
			if (currentLine.length() != 0) {
				currentLine.append(" ");
				space = true;
			}

			int newSize = m.stringWidth((space ? " " : "") + splitst);
			if (currentWidth + newSize <= textWidth) { // no need to wrap
				/* No need to wrap */
				currentLine.append(splitst);
				currentWidth += newSize;
			}
			else { // we need to spit, all the sizes and string reset
				currentLine.setLength(0);
				currentLine.append(splitst);
				currentWidth = m.stringWidth(splitst);
				linesTarget += 1;
			}
		}
		return linesTarget;
	}

}
