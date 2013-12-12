package it.unibz.krdb.obda.protege4.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SQLQueryPainter {

	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;

	private Timer timer;
	private Font plainFont;
	private Style plainStyle;
	private Style boldStyle;
	private Style nonBoldStyle;
	private Style fontSizeStyle;

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
			@Override
			public void insertUpdate(DocumentEvent e) {
				handleDocumentUpdated();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				handleDocumentUpdated();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				// NO-OP
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
		StyledDocument doc = parent.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setBold(plainStyle, false);

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setForeground(boldStyle, new Color(38,128,2));

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

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
		plainFont = new Font("Courier", Font.PLAIN, 14);
		parent.setFont(plainFont);
	}

	/**
	 * Performs coloring of the text pane. This method needs to be rewritten.
	 */
	public void recolorQuery() {
		String input = parent.getText();
		resetStyles();

		doc.setCharacterAttributes(0, doc.getLength(), plainStyle, true);

		// Locating all SELECTS
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
