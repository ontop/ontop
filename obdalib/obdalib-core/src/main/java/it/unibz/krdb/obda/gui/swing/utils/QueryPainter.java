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

import it.unibz.krdb.obda.gui.swing.treemodel.TargetQueryVocabularyValidator;
import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.parser.TurtleSyntaxParser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPainter {

	private Vector<ColorTask> tasks = null;
	private boolean alreadyColoring = false;
	private final OBDAModel apic;

	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;
	private SimpleAttributeSet dataProp;
	private SimpleAttributeSet objectProp;
	private SimpleAttributeSet clazz;

	private SimpleAttributeSet individual;

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
	private TargetQueryVocabularyValidator validator;

	private boolean invalid = false;

	private static final Color INVALID = Color.RED;

	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();

	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();

	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;

	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;

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

	private List<ValidatorListener> validatorListeners = new LinkedList<QueryPainter.ValidatorListener>();
	private TurtleSyntaxParser textParser;

	public QueryPainter(OBDAModel apic, JTextPane parent, TargetQueryVocabularyValidator validator) {
		this.apic = apic;
		this.parent = parent;
		this.validator = validator;
		this.doc = parent.getStyledDocument();
		this.parent = parent;
		textParser = new TurtleSyntaxParser(apic.getPrefixManager());


		prepareStyles();
		setupFont();

		prepareTextPane();

		tasks = new Vector<ColorTask>();

		this.outerBorder = null;
		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		setStateBorder(defaultBorder);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);
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
		parent.getStyledDocument().addDocumentListener(docListener);

		timer = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleTimer();
			}
		});
	}

	public void addValidatorListener(ValidatorListener listener) {
		validatorListeners.add(listener);
	}

	public void removeValidatorListener(ValidatorListener listener) {
		validatorListeners.remove(listener);
	}

	private void fireValidationOcurred() {
		for (ValidatorListener listener : validatorListeners) {
			listener.validated(!invalid);
		}
	}

	public interface ValidatorListener {

		/***
		 * Called when the validator has just validated. Takes as input the
		 * result of the validation.
		 * 
		 * @return
		 */
		public void validated(boolean result);
	}

	private void handleDocumentUpdated() {
		timer.restart();
		clearError();
		// performHighlighting();
	}

	private void handleTimer() {
		timer.stop();
		checkExpression();
		if (!invalid) {
			try {
				recolorQuery();
			} catch (Exception e) {

			}
		}
	}

	/***
	 * Checks if the query is valid. If so, notifies listeners that the query
	 * has been validated.
	 * 
	 * @throws Exception
	 */
	private void validate() throws Exception {
		final String text = parent.getText().trim();
		// final Object node = parent.getEditingObject();

		if (text.isEmpty()) {
			String msg = String.format("Empty query");
			invalid = true;
			throw new Exception(msg);
		}

		CQIE query = null;
		query = textParser.parse(text);

		if (query == null) {
			invalid = true;
			throw parsingException;
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

		invalid = false;

	}

	protected void checkExpression() {
		try {
			// Parse the text in the editor. If no parse
			// exception is thrown, clear the error, otherwise
			// set the error
			validate();
			invalid = false;
			setError(null);
		} catch (Exception e) {
			invalid = true;
			setError(e);
		} finally {
			fireValidationOcurred();
		}
	}

	private void setError(Exception e) {

		if (e != null) {
			ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
			ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
			if (e instanceof IllegalArgumentException)
				parent.setToolTipText("Syntax error");
			else {
				String errorstring = e.getMessage();
				int index = errorstring.indexOf("Location: line");
				if (index != -1) {
					String location = errorstring.substring(index + 15);
					int prefixlines = apic.getPrefixManager().getPrefixMap().keySet().size();
					String[] coordinates = location.split(":");
					
					int errorline = Integer.valueOf(coordinates[0]) - prefixlines;
					int errorcol = Integer.valueOf(coordinates[1]);
					errorstring = errorstring.replace(errorstring.substring(index), "Location: line " + errorline + " column " + errorcol);
				}
				
				
				parent.setToolTipText(getHTMLErrorMessage(errorstring));
			}
			setStateBorder(errorBorder);
			// setErrorRange(e.getStartIndex(), e.getEndIndex());
		} else {
			clearError();
		}
	}

	private static String getHTMLErrorMessage(String msg) {
		if (msg != null) {
			String html = "<html><body>";
			msg = msg.replace("\n", "<br>");
			msg = msg.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
			html += msg;
			html += "</body></html>";
			return html;
		}
		return null;
	}

	private void clearError() {
		parent.setToolTipText(null);
		setStateBorder(defaultBorder);
		// setErrorRange(0, 0);
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}

	public void setStateBorder(Border border) {
		stateBorder = border;
		this.parent.setBorder(BorderFactory.createCompoundBorder(outerBorder, stateBorder));
	}

	private void prepareStyles() {
		// boolean useDefault = new
		// Boolean(pref.get(OBDAPreferences.USE_DEAFAULT).toString());

		StyledDocument doc = parent.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		// StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
		// StyleConstants.setSpaceAbove(plainStyle, 0);

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		invalidQuery = doc.addStyle("INVALID_STYLE", null);
		StyleConstants.setForeground(invalidQuery, INVALID);

		/****
		 * Snippets
		 */

		black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);

		brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);

		String font = "Dialog";
		Integer size = 14;

		dataProp = new SimpleAttributeSet();
		Color c_dp = new Color(41, 167, 121);
		dataProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_dp);
		dataProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		objectProp = new SimpleAttributeSet();
		Color c_op = new Color(41, 119, 167);
		objectProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_op);
		objectProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		clazz = new SimpleAttributeSet();
		Color c_clazz = new Color(199, 155, 41);
		clazz.addAttribute(StyleConstants.CharacterConstants.Foreground, c_clazz);
		clazz.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		individual = new SimpleAttributeSet();
		Color c_individual = new Color(83, 24, 82);
		individual.addAttribute(StyleConstants.CharacterConstants.Foreground, c_individual);
		individual.addAttribute(StyleConstants.CharacterConstants.Bold, true);

	}

	private void resetStyles() {
		doc.setParagraphAttributes(0, doc.getLength(), plainStyle, true);
		StyleConstants.setFontSize(fontSizeStyle, getFontSize());
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

		plainFont = new Font("Dialog", Font.PLAIN, 14);
		// plainFont =
		// Font.getFont(preferences.get(OBDAPreferences.OBDAPREFS_FONTFAMILY).toString().trim());
		plainFontHeight = parent.getFontMetrics(plainFont).getHeight();
		boldFont = plainFont.deriveFont(Font.BOLD);
		parent.setFont(plainFont);
	}

	private int getFontSize() {
		return 14;
		// return
		// Integer.parseInt(pref.get(OBDAPreferences.OBDAPREFS_FONTSIZE).toString());
	}

	/***
	 * Performs coloring of the textpane. This method needs to be rewriten.
	 * 
	 * @throws Exception
	 */
	public void recolorQuery() throws Exception {

		PrefixManager man = apic.getPrefixManager();

		String input = doc.getText(0, doc.getLength());
		CQIE current_query = parse(input);

		if (current_query == null)
			throw new Exception("Unable to parse the query: " + input + ", " + parsingException);

		input = doc.getText(0, doc.getLength());

		int pos = input.indexOf("(", 0);
		while (pos != -1) {
			doc.setCharacterAttributes(pos, 1, brackets, false);
			pos = input.indexOf("(", pos + 1);
		}
		pos = input.indexOf(")", 0);
		while (pos != -1) {
			doc.setCharacterAttributes(pos, 1, brackets, false);
			pos = input.indexOf(")", pos + 1);
		}
		pos = input.indexOf(".", 0);
		while (pos != -1) {
			doc.setCharacterAttributes(pos, 1, black, false);
			pos = input.indexOf(".", pos + 1);
		}
		pos = input.indexOf(",", 0);
		while (pos != -1) {
			doc.setCharacterAttributes(pos, 1, black, false);
			pos = input.indexOf(",", pos + 1);
		}
		pos = input.indexOf(":", 0);
		while (pos != -1) {
			doc.setCharacterAttributes(pos, 1, black, false);
			pos = input.indexOf(":", pos + 1);
		}

		for (Atom atom : current_query.getBody()) {
			Predicate predicate = atom.getPredicate();
			String predicateName = man.getShortForm(atom.getPredicate().toString());

			if (validator.isClass(predicate)) {
				ColorTask task = new ColorTask(predicateName, clazz);
				tasks.add(task);
			} else if (validator.isObjectProperty(predicate)) {
				ColorTask task = new ColorTask(predicateName, objectProp);
				tasks.add(task);
			} else if (validator.isDataProperty(predicate)) {
				ColorTask task = new ColorTask(predicateName, dataProp);
				tasks.add(task);
			}

			Term term1 = null;
			Term term2 = null;
			term1 = atom.getTerm(0);
			if (atom.getArity() == 2) {
				term2 = atom.getTerm(1);
			}
			if (term1 instanceof URIConstant) {
				String rendered = man.getShortForm(((URIConstant) term1).getURI().toString());
				ColorTask task = new ColorTask(rendered, individual);
				tasks.add(task);
			}
			if (term2 instanceof URIConstant) {
				String rendered = man.getShortForm(((URIConstant) term2).getURI().toString());
				ColorTask task = new ColorTask(rendered, individual);
				tasks.add(task);
			}

		}

		ColorTask[] taskArray = order(tasks);
		for (int i = 0; i < taskArray.length; i++) {
			if (taskArray[i].text != null) {
				int index = input.indexOf(taskArray[i].text, 0);
				while (index != -1) {
					doc.setCharacterAttributes(index, taskArray[i].text.length(), taskArray[i].set, false);
					index = input.indexOf(taskArray[i].text, index + 1);
				}
			}
		}
		tasks.clear();
	}

	private CQIE parse(String query) {
		TurtleSyntaxParser textParser = new TurtleSyntaxParser(apic.getPrefixManager());
		try {
			return textParser.parse(query);
		} catch (RecognitionException e) {
			parsingException = e;
			// log.warn(e.getMessage(), e);
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private ColorTask[] order(Vector<ColorTask> v) {

		ColorTask[] s = new ColorTask[v.size()];
		ColorTask[] result = v.toArray(s);

		for (int i = 1; i < result.length; i++) {
			int j = i;
			ColorTask str = result[i];
			int B = result[i].text.length();
			while ((j > 0) && (result[j - 1].text != null) && (result[j - 1].text.length() > B)) {
				result[j] = result[j - 1];
				j--;
			}
			result[j] = str;
		}
		return result;
	}

	public boolean isValidQuery() {
		return !invalid;
	}

	class ColorTask {

		public String text = null;
		public SimpleAttributeSet set = null;

		public ColorTask(String s, SimpleAttributeSet sas) {

			text = s;
			set = sas;
		}
	}
}
