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
import it.unibz.inf.ontop.protege.core.*;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import org.apache.commons.rdf.api.IRI;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TargetQueryPainter {
	private final OBDAModelManager obdaModelManager;

	private final Border defaultBorder;
	private final Border errorBorder;
	private final Timer timer;

	private boolean invalid = false;

	private static final int DEFAULT_TOOL_TIP_INITIAL_DELAY = ToolTipManager.sharedInstance().getInitialDelay();
	private static final int DEFAULT_TOOL_TIP_DISMISS_DELAY = ToolTipManager.sharedInstance().getDismissDelay();
	private static final int ERROR_TOOL_TIP_INITIAL_DELAY = 100;
	private static final int ERROR_TOOL_TIP_DISMISS_DELAY = 9000;

	private final JTextPane textPane;

	private final List<ValidatorListener> validatorListeners = new LinkedList<>();

	public TargetQueryPainter(OBDAModelManager obdaModelManager, JTextPane textPane) {
		this.obdaModelManager = obdaModelManager;
		this.textPane = textPane;

		defaultBorder = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY);
		errorBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED);

		timer = new Timer(200, e -> handleTimer());

		textPane.getStyledDocument().addDocumentListener(new DocumentListener() {
				@Override public void insertUpdate(DocumentEvent e1) { handleDocumentUpdated(); }
				@Override public void removeUpdate(DocumentEvent e1) { handleDocumentUpdated(); }
				@Override public void changedUpdate(DocumentEvent e1) { handleDocumentUpdated(); }
			});
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
	}


	protected void checkExpression() {
		try {
			String text = textPane.getText().trim();
			if (text.isEmpty() || text.length() == 1) {
				throw new Exception("Empty query");
			}

			ImmutableList<TargetAtom> query = obdaModelManager.getActiveOBDAModel().parseTargetQuery(text);
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
		textPane.setBorder(BorderFactory.createCompoundBorder(null, errorBorder));
		ToolTipManager.sharedInstance().setInitialDelay(ERROR_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(ERROR_TOOL_TIP_DISMISS_DELAY);
		textPane.setToolTipText(tooltip);
	}

	private void clearError() {
		invalid = false;
		textPane.setToolTipText(null);
		textPane.setBorder(BorderFactory.createCompoundBorder(null, defaultBorder));
		ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_TOOL_TIP_INITIAL_DELAY);
		ToolTipManager.sharedInstance().setDismissDelay(DEFAULT_TOOL_TIP_DISMISS_DELAY);
	}
}
