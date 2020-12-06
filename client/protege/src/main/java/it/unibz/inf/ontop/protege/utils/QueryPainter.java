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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.TargetQueryParserException;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.protege.core.MutableOntologyVocabulary;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.TargetQueryValidator;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import org.apache.commons.rdf.api.IRI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryPainter {
	private final OBDAModel apic;

	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;
	private SimpleAttributeSet dataProp;
	private SimpleAttributeSet objectProp;
	private SimpleAttributeSet annotProp;
	private SimpleAttributeSet clazz;
	private SimpleAttributeSet individual;

	private Style invalidQuery;
	private Border defaultBorder;
	private Border outerBorder;
	private Border stateBorder;
	private Border errorBorder;
	private Timer timer;
	private TargetQueryParserException parsingException;

	private boolean invalid = false;

	private static final Color INVALID = Color.RED;
	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;

	private Font plainFont;
	private Style plainStyle;
	private Style boldStyle;
	private Style nonBoldStyle;
	private Style foreground;
	private Style fontSizeStyle;

	private StyledDocument doc = null;
	private JTextPane parent = null;
	private Vector<ColorTask> tasks = new Vector<ColorTask>();

	private List<ValidatorListener> validatorListeners = new LinkedList<QueryPainter.ValidatorListener>();

	public QueryPainter(OBDAModel apic, JTextPane parent) {
		this.apic = apic;
		this.parent = parent;
		this.doc = parent.getStyledDocument();
		this.parent = parent;

		prepareStyles();
		prepareTextPane();
		prepareTimer();
		prepareBorder();
	}

	private void prepareBorder() {
		outerBorder = null;
		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);
		setStateBorder(defaultBorder);
	}

	private void prepareTimer() {
		timer = new Timer(200, new ActionListener() {
			@Override
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

	private void fireValidationOccurred() {
		for (ValidatorListener listener : validatorListeners) {
			listener.validated(!invalid);
		}
	}

	public interface ValidatorListener {
		/**
		 * Called when the validator has just validated. Takes as input the
		 * result of the validation.
		 */
		public void validated(boolean result);
	}

	private void handleDocumentUpdated() {
		timer.restart();
		clearError();
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
		if (text.isEmpty() || text.length() == 1) {
			invalid = true;
			throw new Exception("Empty query");
		}

		TargetQueryParser textParser = apic.createTargetQueryParser();
		ImmutableList<TargetAtom> query = textParser.parse(text);

		if (query == null) {
			invalid = true;
			throw parsingException;
		}
		ImmutableList<IRI> invalidPredicates = TargetQueryValidator.validate(query, apic.getCurrentVocabulary());
		if (!invalidPredicates.isEmpty()) {
			invalid = true;
			throw new Exception("ERROR: The below list of predicates is unknown by the ontology: \n "
					+ invalidPredicates.stream()
						.map(iri -> "- " + iri + "\n")
						.collect(Collectors.joining())
					+ " Note: null indicates an unknown prefix.");
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
			fireValidationOccurred();
		}
	}

	private void setError(Exception e) {
		if (e != null) {
			ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
			ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
			if (e instanceof IllegalArgumentException) {
				parent.setToolTipText("Syntax error");
			}
			else {

				Throwable cause = e.getCause();
				String errorstring;
				if(cause!=null) {
					errorstring = cause.getMessage();
				}
				else {
					errorstring = e.getMessage();
				}

				if(errorstring != null) {

					parent.setToolTipText(getHTMLErrorMessage(errorstring));
				}
				else{
					parent.setToolTipText("Syntax error, check log");
				}

			}
			setStateBorder(errorBorder);
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
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}

	public void setStateBorder(Border border) {
		stateBorder = border;
		this.parent.setBorder(BorderFactory.createCompoundBorder(outerBorder, stateBorder));
	}

	private void prepareStyles() {
		StyledDocument doc = parent.getStyledDocument();
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		StyleConstants.setItalic(plainStyle, false);

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		invalidQuery = doc.addStyle("INVALID_STYLE", null);
		StyleConstants.setForeground(invalidQuery, INVALID);

		foreground = doc.addStyle("FOREGROUND", null);
		StyleConstants.setForeground(foreground, Color.black);

		black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);

		brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);

		annotProp = new SimpleAttributeSet();
		Color c_dp = new Color(109, 159, 162);
		annotProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_dp);
		annotProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		dataProp = new SimpleAttributeSet();
		Color c_ap = new Color(41, 167, 121);
		dataProp.addAttribute(StyleConstants.CharacterConstants.Foreground, c_ap);
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
		setupFont();
		StyleConstants.setFontSize(fontSizeStyle, getFontSize());
		StyleConstants.setFontFamily(fontSizeStyle, plainFont.getFamily());
		StyleConstants.setForeground(fontSizeStyle, Color.black);
		fontSizeStyle.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, true);
	}

	private void prepareTextPane() {
		resetStyles();
		parent.setBorder(null);
		parent.getStyledDocument().addDocumentListener(
			new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e) { handleDocumentUpdated(); }
				@Override public void removeUpdate(DocumentEvent e) { handleDocumentUpdated(); }
				@Override public void changedUpdate(DocumentEvent e) { /* NO-OP */ }
			}
		);
	}

	private void setupFont() {
		plainFont = new Font("Lucida Grande", Font.PLAIN, 14);
		parent.setFont(plainFont);
	}

	private int getFontSize() {
		return 14;
	}

	/***
	 * Performs coloring of the textpane. This method needs to be rewriten.
	 * 
	 * @throws Exception
	 */
	public void recolorQuery() throws Exception {
		PrefixManager man = apic.getMutablePrefixManager();

		String input = doc.getText(0, doc.getLength());
		ImmutableList<TargetAtom> current_query = parse(input, man);

		if (current_query == null) {
            JOptionPane.showMessageDialog(null, "An error occured while parsing the mappings. For more info, see the logs.");
			throw new Exception("Unable to parse the mapping target: " + input + ", " + parsingException);
		}
		input = doc.getText(0, doc.getLength());

		resetStyles();

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
		MutableOntologyVocabulary vocabulary = apic.getCurrentVocabulary();
		for (TargetAtom atom : current_query) {
			Optional<IRI> optionalPredicateIri = atom.getPredicateIRI();

			if (optionalPredicateIri.isPresent()) {
				IRI predicateIri = optionalPredicateIri.get();
				String shortIRIForm = man.getShortForm("<"+predicateIri.getIRIString()+">");

				if (vocabulary.classes().contains(predicateIri)) {
					ColorTask task = new ColorTask(shortIRIForm, clazz);
					tasks.add(task);
				} else if (vocabulary.objectProperties().contains(predicateIri)) {
					ColorTask task = new ColorTask(shortIRIForm, objectProp);
					tasks.add(task);
				} else if (vocabulary.dataProperties().contains(predicateIri)) {
					ColorTask task = new ColorTask(shortIRIForm, dataProp);
					tasks.add(task);
				} else if (vocabulary.annotationProperties().contains(predicateIri)) {
					ColorTask task = new ColorTask(shortIRIForm, annotProp);
					tasks.add(task);
				}
			}

			ImmutableList<ImmutableTerm> substitutedTerms = atom.getSubstitutedTerms();
			RDFAtomPredicate atomPredicate = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();

			ImmutableTerm term1 = atomPredicate.getSubject(substitutedTerms);
			ImmutableTerm term2 = atomPredicate.getObject(substitutedTerms);

			if (term1 instanceof IRIConstant) {
				String rendered = man.getShortForm(((IRIConstant) term1).getIRI().toString());
				ColorTask task = new ColorTask(rendered, individual);
				tasks.add(task);
			}
			if (term2 instanceof IRIConstant) {
				String rendered = man.getShortForm(((IRIConstant) term2).getIRI().toString());
				ColorTask task = new ColorTask(rendered, individual);
				tasks.add(task);
			}
		}

		ColorTask[] taskArray = order(tasks);
		for (ColorTask ct: taskArray){
			if (ct.text != null) {
				Matcher matcher = Pattern.compile("\\s("+ct.text+")[\\s\\.;,]")
						.matcher(input);
				while (matcher.find()){
					doc.setCharacterAttributes(matcher.start(1), ct.text.length(), ct.set, true);
				}
			}
		}
		tasks.clear();
	}

	private ImmutableList<TargetAtom> parse(String query, PrefixManager man) {
		try {
            TargetQueryParser textParser = apic.createTargetQueryParser(man);
			return textParser.parse(query);
		} catch (TargetQueryParserException e) {
			parsingException = e;
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
