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
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;
import org.apache.commons.rdf.api.IRI;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TargetQueryPainter {
	private final OBDAModel apic;
	private final OBDAModelManager obdaModelManager;

	private SimpleAttributeSet black;
	private SimpleAttributeSet brackets;
	private SimpleAttributeSet dataProp;
	private SimpleAttributeSet objectProp;
	private SimpleAttributeSet annotProp;
	private SimpleAttributeSet clazz;
	private SimpleAttributeSet individual;

	private final Border defaultBorder;
	private final Border errorBorder;
	private final Timer timer;

	private boolean invalid = false;

	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;

	private final Style fontSizeStyle;

	private final StyledDocument doc;
	private final JTextPane parent;

	private final List<ValidatorListener> validatorListeners = new LinkedList<>();

	public TargetQueryPainter(OBDAModelManager obdaModelManager, JTextPane parent) {
		this.obdaModelManager = obdaModelManager;
		this.apic = obdaModelManager.getActiveOBDAModel();
		this.parent = parent;
		this.doc = parent.getStyledDocument();

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);

		black = new SimpleAttributeSet();
		black.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);

		brackets = new SimpleAttributeSet();
		brackets.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.BLACK);

		annotProp = new SimpleAttributeSet();
		annotProp.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(109, 159, 162));
		annotProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		dataProp = new SimpleAttributeSet();
		dataProp.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(41, 167, 121));
		dataProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		objectProp = new SimpleAttributeSet();
		objectProp.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(41, 119, 167));
		objectProp.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		clazz = new SimpleAttributeSet();
		clazz.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(199, 155, 41));
		clazz.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		individual = new SimpleAttributeSet();
		individual.addAttribute(StyleConstants.CharacterConstants.Foreground, new Color(83, 24, 82));
		individual.addAttribute(StyleConstants.CharacterConstants.Bold, true);

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		parent.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));

		timer = new Timer(200, e -> handleTimer());

		parent.getStyledDocument().addDocumentListener(new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e1) { handleDocumentUpdated(); }
				@Override public void removeUpdate(DocumentEvent e1) { handleDocumentUpdated(); }
				@Override public void changedUpdate(DocumentEvent e1) { /* NO-OP */ }
			});

		resetStyles();
	}

	public void addValidatorListener(ValidatorListener listener) {
		validatorListeners.add(listener);
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
		void validated(boolean result);
	}

	private void handleDocumentUpdated() {
		timer.restart();
		clearError();
	}

	private void handleTimer() {
		timer.stop();
		checkExpression();
		if (!invalid) {
			recolorQuery();
		}
	}


	protected void checkExpression() {
		try {
			String text = parent.getText().trim();
			if (text.isEmpty() || text.length() == 1) {
				throw new Exception("Empty query");
			}

			ImmutableList<TargetAtom> query = apic.parseTargetQuery(text);
			ImmutableList<IRI> invalidPredicates = obdaModelManager.getCurrentVocabulary().validate(query);
			if (!invalidPredicates.isEmpty()) {
				throw new Exception("ERROR: The below list of predicates is unknown by the ontology: \n "
						+ invalidPredicates.stream()
						.map(iri -> "- " + iri + "\n")
						.collect(Collectors.joining())
						+ " Note: null indicates an unknown prefix.");
			}
			clearError();
		}
		catch (IllegalArgumentException e) {
			setError("Syntax error");
		}
		catch (Exception e) {
				Throwable cause = e.getCause();
				String error = (cause != null) ? cause.getMessage() : e.getMessage();
				if (error != null) {
					setError("<html><body>" +
							error.replace("\n", "<br>")
								 .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")
							+ "</body></html>");
				}
				else {
					setError("Syntax error, check log");
				}
		}
		finally {
			fireValidationOccurred();
		}
	}

	private void setError(String tooltip) {
		invalid = true;
		parent.setBorder(BorderFactory.createCompoundBorder(null, errorBorder));
		ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
		parent.setToolTipText(tooltip);
	}

	private void clearError() {
		invalid = false;
		parent.setToolTipText(null);
		parent.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}

	private void resetStyles() {
		Font plainFont = new Font("Lucida Grande", Font.PLAIN, 14);
		parent.setFont(plainFont);
		StyleConstants.setFontSize(fontSizeStyle, plainFont.getSize());
		StyleConstants.setFontFamily(fontSizeStyle, plainFont.getFamily());
		StyleConstants.setForeground(fontSizeStyle, Color.black);
		fontSizeStyle.addAttribute(StyleConstants.CharacterConstants.Foreground, Color.black);
		doc.setParagraphAttributes(0, doc.getLength(), fontSizeStyle, true);
	}


	/***
	 * Performs coloring of the textpane.
	 */
	public void recolorQuery() {
		ImmutableList<TargetAtom> current_query;
		try {
			String input = doc.getText(0, doc.getLength());
			current_query = apic.parseTargetQuery(input);
		}
		catch (Exception e) {
			return;
			//if (current_query == null) {
			//	JOptionPane.showMessageDialog(null, "An error occurred while parsing the mappings. For more info, see the logs.");
			//	throw new Exception("Unable to parse the mapping target: " + input + ", " + parsingException);
			//}
		}

		resetStyles();

		highlight("(", brackets);
		highlight(")", brackets);
		highlight(".", black);
		highlight(",", black);
		highlight(":", black);

		OntologySignature vocabulary = obdaModelManager.getCurrentVocabulary();
		PrefixManager prefixManager = apic.getMutablePrefixManager();
		for (TargetAtom atom : current_query) {
			ImmutableList<ImmutableTerm> substitutedTerms = atom.getSubstitutedTerms();
			RDFAtomPredicate atomPredicate = (RDFAtomPredicate) atom.getProjectionAtom().getPredicate();

			ImmutableTerm term1 = atomPredicate.getSubject(substitutedTerms);
			if (term1 instanceof IRIConstant) {
				String rendered = prefixManager.getShortForm(((IRIConstant) term1).getIRI().toString());
				highlight(rendered, individual);
			}

			ImmutableTerm term2 = atomPredicate.getProperty(substitutedTerms);
			if (term2 instanceof IRIConstant) {
				IRI predicateIri = ((IRIConstant)term2).getIRI();
				String shortIRIForm = prefixManager.getShortForm(predicateIri.toString());
				if (shortIRIForm.equals("rdf:type")) {
					ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
					if (term3 instanceof IRIConstant) {
						IRI classIri = ((IRIConstant) term3).getIRI();
						if (vocabulary.containsClass(classIri))
							highlight(prefixManager.getShortForm(classIri.toString()), clazz);
					}
				}
				else {
					if (vocabulary.containsObjectProperty(predicateIri))
						highlight(shortIRIForm, objectProp);
					else if (vocabulary.containsDataProperty(predicateIri))
						highlight(shortIRIForm, dataProp);
					else if (vocabulary.containsAnnotationProperty(predicateIri))
						highlight(shortIRIForm, annotProp);

					ImmutableTerm term3 = atomPredicate.getObject(substitutedTerms);
					if (term3 instanceof IRIConstant) {
						String rendered = prefixManager.getShortForm(((IRIConstant) term3).getIRI().toString());
						highlight(rendered, individual);
					}
				}
			}
		}
	}

	private void highlight(String s, SimpleAttributeSet attributeSet) {
		try {
			String input = doc.getText(0, doc.getLength());
			int total = input.length();
			int len = s.length();
			int pos = input.indexOf(s, 0);
			while (pos != -1) {
				if (len == 1 || (pos == 0 || isDelimiter(input.charAt(pos - 1))) &&
						(pos + len == total || isDelimiter(input.charAt(pos + len))))
					doc.setCharacterAttributes(pos, len, attributeSet, false);
				pos = input.indexOf(s, pos + len);
			}
		}
		catch (BadLocationException e) {
			// NO-OP
		}
	}

	private static boolean isDelimiter(char c) {
		return Character.isWhitespace(c) || c == '.' || c == ';' || c == ',';
	}
}
