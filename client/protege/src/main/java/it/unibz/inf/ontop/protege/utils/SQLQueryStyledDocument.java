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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.text.*;

public class SQLQueryStyledDocument extends DefaultStyledDocument {

	private static final Font SQL_FONT = new Font("Courier", Font.PLAIN, 14);
	private static final Color SQL_COLOR = new Color(38,128,2);

	private final SimpleAttributeSet plainStyle = new SimpleAttributeSet();
	private final SimpleAttributeSet boldStyle = new SimpleAttributeSet();

	public SQLQueryStyledDocument() {
		StyleConstants.setFontSize(plainStyle, SQL_FONT.getSize());
		StyleConstants.setFontFamily(plainStyle, SQL_FONT.getFamily());

		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setForeground(boldStyle, SQL_COLOR);
	}

	@Override
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offset, str, a);
		highlightKeywords();
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);
		highlightKeywords();
	}

	private void highlightKeywords() throws BadLocationException {
		setCharacterAttributes(0, getLength(), plainStyle, true);
		highlight("SELECT");
		highlight("FROM");
		highlight("WHERE");
		highlight("ON");
		highlight("JOIN");
		highlight("AND");
		highlight("AS");
	}

	private void highlight(String keyword) throws BadLocationException {
		int length = keyword.length();
		String pattern = IntStream.range(0, length)
				.mapToObj(keyword::charAt)
				.map(c -> "[" + Character.toUpperCase(c) + Character.toLowerCase(c) + "]")
				.collect(Collectors.joining("", "(", ")"));
		String input = getText(0, getLength()).replaceAll(pattern, keyword.toUpperCase());
		int total = input.length();
		int index, offset = 0;
		while ((index = input.indexOf(keyword, offset)) != -1) {
			offset = index + length;
			if ((index == 0 || Character.isWhitespace(input.charAt(index - 1))) &&
					(offset >= total || Character.isWhitespace(input.charAt(offset))))
				setCharacterAttributes(index, length, boldStyle, false);
		}
	}
}
