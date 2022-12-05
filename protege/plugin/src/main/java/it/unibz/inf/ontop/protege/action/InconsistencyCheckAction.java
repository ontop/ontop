package it.unibz.inf.ontop.protege.action;

/*
 * #%L
 * ontop-protege4
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

import it.unibz.inf.ontop.protege.core.OntopProtegeReasoner;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.Optional;

public class InconsistencyCheckAction extends ProtegeAction {
	
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(InconsistencyCheckAction.class);

	private static final String DIALOG_TITlE = "Consistency checking";

	@Override
	public void actionPerformed(ActionEvent evt) {
		Optional<OntopProtegeReasoner> reasoner = DialogUtils.getOntopProtegeReasoner(getEditorKit());
		if (!reasoner.isPresent())
			return;

		try {
			OntopProtegeReasoner ontop = reasoner.get();
			boolean isConsistent = ontop.isQuestConsistent();
			LOGGER.debug("Consistency checking returned: " + isConsistent);
			if (isConsistent) {
				DialogUtils.showInfoDialog(getWorkspace(),
						"Your ontology is consistent! Top job!",
						DIALOG_TITlE);
			}
			else {
				DialogUtils.showInfoDialog(getWorkspace(),
						"Your ontology is not consistent.\n" +
								"The axiom causing inconsistency is:\n" +
								ontop.getInconsistentAxiom(),
						DIALOG_TITlE);
			}
		}
		catch (Throwable e) {
			DialogUtils.showQuickErrorDialog(getWorkspace(), e, DIALOG_TITlE + " error.");
		}
	}

	@Override
	public void initialise()  { /* NO-OP */ }

	@Override
	public void dispose()  {/* NO-OP */}
}
