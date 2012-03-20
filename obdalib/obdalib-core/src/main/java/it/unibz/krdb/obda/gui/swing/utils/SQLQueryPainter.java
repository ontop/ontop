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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLQueryPainter {

	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;
	private SimpleAttributeSet parameters;

	private Timer timer;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private Font plainFont;
	private int plainFontHeight;
	private Font boldFont;
	private Style plainStyle;
	private Style boldStyle;
	private Style nonBoldStyle;
	private Style selectionForeground;
	private Style foreground;
	private Style fontSizeStyle;
	private JTree componentBeingRendered;

	StyledDocument doc = null;
	JTextPane parent = null;
	private DocumentListener docListener;

	public SQLQueryPainter(JTextPane parent) {

		this.doc = parent.getStyledDocument();
		this.parent = parent;

		prepareStyles();
		setupFont();

		prepareTextPane();
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
		parent.getStyledDocument().addDocumentListener(docListener);

		timer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleTimer();
			}
		});
	}

	private void handleDocumentUpdated() {
		timer.restart();
	}

	private void handleTimer() {
		recolorQuery();
	}

	private void prepareStyles() {
		// boolean useDefault = new
		// Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString());

		StyledDocument doc = parent.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		// StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setBold(plainStyle, false);
		// StyleConstants.setSpaceAbove(plainStyle, 0);

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setForeground(boldStyle, new Color(38,128,2));

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		/****
		 * Snippets
		 */

		black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);

		brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);
	}

	private void resetStyles() {
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
		StyleConstants.setFontSize(fontSizeStyle, 14);
		Font f = plainFont;
		StyleConstants.setFontFamily(fontSizeStyle, f.getFamily());
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, false);
		setupFont();

	}

	private void prepareTextPane() {
		parent.setBorder(null);
		doc = parent.getStyledDocument();
		resetStyles();
	}

	private void setupFont() {

		// StyleConstants.setFontFamily(style, );
		// StyleConstants.setFontSize(style,
		// Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString()));

		plainFont = new Font("Courier", Font.PLAIN, 14);
		// plainFont =
		// Font.getFont(preferences.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString().trim());
		plainFontHeight = parent.getFontMetrics(plainFont).getHeight();
		boldFont = plainFont.deriveFont(Font.BOLD);
		parent.setFont(plainFont);
	}

	/***
	 * Performs coloring of the textpane. This method needs to be rewriten.
	 * 
	 * @throws Exception
	 */
	public void recolorQuery() {
		String input = parent.getText();
		resetStyles();
		// doc.getText(0, doc.getLength());

		doc.setCharacterAttributes(0, doc.getLength(), plainStyle, true);


		/* Locating all SELECTS */

		input = input.replaceAll("[Ss][Ee][Ll][Ee][Cc][Tt]", "SELECT");
		int offset = 0;
		for (int index = input.indexOf("SELECT", offset); index != -1; index = input.indexOf("SELECT", offset)) {
			doc.setCharacterAttributes(index, index + 6, boldStyle, true);
			offset = index + 6;
			doc.setCharacterAttributes(offset, offset  +1, plainStyle, true);
		}
		

		input = input.replaceAll("([Ff][Rr][Oo][Mm])", "FROM");
		offset = 0;
		for (int index = input.indexOf("FROM", offset); index != -1; index = input.indexOf("FROM", offset)) {
			doc.setCharacterAttributes(index, index + 4, boldStyle, false);
			offset = index + 4 ;
			doc.setCharacterAttributes(offset, offset + 1, plainStyle, true);
		}

		input = input.replaceAll("([Ww][hH][Ee][Rr][Ee])", "WHERE");
		offset = 0;
		for (int index = input.indexOf("WHERE", offset); index != -1; index = input.indexOf("WHERE", offset)) {
			doc.setCharacterAttributes(index, index + 5, boldStyle, false);
			offset = index + 5;
			doc.setCharacterAttributes(offset, offset + 1, plainStyle, true);
		}
		
		parent.revalidate();

	}

}
