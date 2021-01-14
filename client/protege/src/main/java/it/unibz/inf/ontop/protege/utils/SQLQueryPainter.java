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

import java.awt.Color;
import java.awt.Font;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SQLQueryPainter {

	private final Timer timer;

	private final Font plainFont;
	private final Style plainStyle;
	private final Style boldStyle;
	private final Style fontSizeStyle;

	private final StyledDocument doc;
	private final JTextPane parent;

	public SQLQueryPainter(JTextPane parent) {
		this.parent = parent;
		this.doc = parent.getStyledDocument();

		plainFont = new Font("Courier", Font.PLAIN, 14);

		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setBold(plainStyle, false);

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setForeground(boldStyle, new Color(38,128,2));

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		parent.setBorder(null);

		timer = new Timer(200, e -> recolorQuery());

		parent.getStyledDocument().addDocumentListener(new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { timer.restart(); }
			@Override public void removeUpdate(DocumentEvent e) { timer.restart(); }
			@Override public void changedUpdate(DocumentEvent e) { /* NO-OP */}
		});

		resetStyles();
	}

	private void resetStyles() {
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
		StyleConstants.setFontSize(fontSizeStyle, plainFont.getSize());
		StyleConstants.setFontFamily(fontSizeStyle, plainFont.getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, false);

		parent.setFont(plainFont);
	}

	/**
	 * Performs coloring of the text pane. This method needs to be rewritten.
	 */
	public void recolorQuery() {
		resetStyles();

		doc.setCharacterAttributes(0, doc.getLength(), plainStyle, true);
		highlight("SELECT");
		highlight("FROM");
		highlight("WHERE");
		parent.revalidate();
	}

	private void highlight(String keyword) {
		String pattern = IntStream.range(0, keyword.length())
				.mapToObj(keyword::charAt)
				.map(c -> "[" + Character.toUpperCase(c) + Character.toLowerCase(c) + "]")
				.collect(Collectors.joining("", "(", ")"));
		String input = parent.getText().replaceAll(pattern, keyword.toUpperCase());
		int len = keyword.length();
		int offset = 0;
		for (int index = input.indexOf(keyword, offset); index != -1; index = input.indexOf(keyword, offset)) {
			offset = index + len + 1;
			doc.setCharacterAttributes(index, offset, boldStyle, false);
			doc.setCharacterAttributes(offset, offset + 1, plainStyle, true);
		}
	}
}
