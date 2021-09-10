package it.unibz.inf.ontop.protege.mapping;

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

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.utils.ColorSettings;
import it.unibz.inf.ontop.protege.utils.SQLQueryStyledDocument;
import it.unibz.inf.ontop.protege.utils.TargetQueryStyledDocument;
import org.protege.editor.owl.OWLEditorKit;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Arrays;

public class MappingListRenderer extends JPanel implements ListCellRenderer<TriplesMap> {

	private final TargetQueryStyledDocument trgQueryDocument;
	private final JTextPane mapTextPane;
	private final JTextPane trgQueryTextPane;
	private final JTextPane srcQueryTextPane;
	private final JLabel statusLabel;
	private final JPanel statusPanel;
	private final JPanel mainPanel;

	private final Font plainFont;
	private final int plainFontHeight;
	private final int plainFontWidth;

	private static final ImmutableMap<TriplesMap.Status, Color> VALIDITY_BACKGROUND = ImmutableMap.of(
			TriplesMap.Status.VALID, new Color(19, 139, 114),
			TriplesMap.Status.NOT_VALIDATED, Color.GRAY.brighter(),
			TriplesMap.Status.INVALID, new Color(235, 28, 93));

	private static final ImmutableMap<TriplesMap.Status, String> VALIDITY_TEXT = ImmutableMap.of(
			TriplesMap.Status.VALID, "V",
			TriplesMap.Status.NOT_VALIDATED, "",
			TriplesMap.Status.INVALID, "I");

	private final SimpleAttributeSet mappingIdStyle = new SimpleAttributeSet();

	private static final int PADDING = 2;
	private static final int MARGIN = 10;
	private static final int BORDER = 1;
	private static final int SEPARATOR = 2;
	private static final int STATUS_WIDTH = 2;

	private final ColorSettings colorSettings;

	public MappingListRenderer(OWLEditorKit editorKit) {
		super(new SpringLayout());

		this.colorSettings = OBDAEditorKitSynchronizerPlugin.getColorSettings(editorKit);

		mapTextPane = new JTextPane();
		mapTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		mapTextPane.setOpaque(false);

		trgQueryTextPane = new JTextPane();
		trgQueryTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		trgQueryTextPane.setOpaque(false);
		trgQueryDocument = new TargetQueryStyledDocument(
				OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit),
				OBDAEditorKitSynchronizerPlugin.getColorSettings(editorKit),
				doc -> {
					try {
						doc.validate();
					}
					catch (TargetQueryParserException ignore) {
					}
				});
		trgQueryTextPane.setDocument(trgQueryDocument);

		srcQueryTextPane = new JTextPane();
		srcQueryTextPane.setMargin(new Insets(SEPARATOR, MARGIN, SEPARATOR, MARGIN));
		srcQueryTextPane.setOpaque(false);
		srcQueryTextPane.setDocument(new SQLQueryStyledDocument());

		mainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mainPanel.add(mapTextPane);
		mainPanel.add(trgQueryTextPane);
		mainPanel.add(srcQueryTextPane);

		JPanel mainPanelPluStatus = new JPanel(new BorderLayout());
		mainPanelPluStatus.add(mainPanel, BorderLayout.CENTER);

		statusLabel = new JLabel("V");
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(statusLabel, BorderLayout.CENTER);
		mainPanelPluStatus.add(statusPanel, BorderLayout.EAST);

		mainPanelPluStatus.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192), BORDER));
		mainPanelPluStatus.setOpaque(true);

		SpringLayout layout = (SpringLayout)getLayout();
		add(mainPanelPluStatus);
		layout.putConstraint(SpringLayout.WEST, mainPanel, PADDING, SpringLayout.WEST, this);
		layout.putConstraint(SpringLayout.NORTH, mainPanel, PADDING, SpringLayout.NORTH, this);

		setOpaque(false);

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
	}


	private void setAttributes(JTextPane pane, SimpleAttributeSet attributes, boolean replace) {
		StyledDocument doc = pane.getStyledDocument();
		doc.setParagraphAttributes(0, doc.getLength(), attributes, replace);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends TriplesMap> list, TriplesMap triplesMap, int index, boolean isSelected, boolean cellHasFocus) {

		SimpleAttributeSet foreground = new SimpleAttributeSet();
		Color fgColor = colorSettings.getForeground(isSelected, ColorSettings.Category.PLAIN);
		if (fgColor != null)
			StyleConstants.setForeground(foreground, fgColor);

		trgQueryDocument.setInvalidPlaceholders(triplesMap.getInvalidPlaceholders());
		trgQueryDocument.setSelected(isSelected);
		trgQueryTextPane.setText(triplesMap.getTargetRendering());
		setAttributes(trgQueryTextPane, foreground,false);

		srcQueryTextPane.setText(triplesMap.getSqlQuery());
		setToolTipText(triplesMap.getSqlErrorMessage());
		setAttributes(srcQueryTextPane, foreground,false);

		mapTextPane.setText(triplesMap.getId());
		setAttributes(mapTextPane, mappingIdStyle, true);
		setAttributes(mapTextPane, foreground,false);

		statusLabel.setText(VALIDITY_TEXT.get(triplesMap.getStatus()));

		statusPanel.setBackground(VALIDITY_BACKGROUND.get(triplesMap.getStatus()));
		mainPanel.setBackground(colorSettings.getBackground(isSelected));

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
		int preferredWidth = list.getParent().getWidth();
		if (preferredWidth == 0) { // JViewport returns 0 when not visible (?)
			preferredWidth = list.getParent().getParent().getWidth();
		}

		int mainPanelWidth = preferredWidth - 2 * BORDER - 2 * PADDING - plainFontWidth * STATUS_WIDTH;
		int textWidth = mainPanelWidth - 2 * MARGIN;
		int minTextHeight = plainFontHeight + 2 * SEPARATOR;

		mapTextPane.setPreferredSize(new Dimension(textWidth, minTextHeight));

		int textTargetHeight = minTextHeight * getTargetQueryLinesNumber(textWidth);
		trgQueryTextPane.setPreferredSize(new Dimension(textWidth, textTargetHeight));
		
		int maxChars = textWidth / plainFontWidth;
		int linesSource = Arrays.stream(srcQueryTextPane.getText().split("\\r?\\n"))
				.mapToInt(line -> (int) (Math.ceil(line.length() / (double) maxChars)))
				.sum();
		int textSourceHeight = minTextHeight * linesSource;
		srcQueryTextPane.setPreferredSize(new Dimension(textWidth, textSourceHeight));

		int totalHeight = minTextHeight + textSourceHeight + textTargetHeight + 2 * BORDER;
		mainPanel.setPreferredSize(new Dimension(mainPanelWidth, totalHeight));
		statusLabel.setPreferredSize(new Dimension(STATUS_WIDTH * plainFontWidth, totalHeight));
		setPreferredSize(new Dimension(preferredWidth, totalHeight + 2 * PADDING));

		revalidate();
		return this;
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
