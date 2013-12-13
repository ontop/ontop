package it.unibz.krdb.obda.protege4.gui.action;

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

import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InconsistencyCheckAction extends ProtegeAction {
	
	private static final long serialVersionUID = 1L;
	private OWLEditorKit editorKit = null;
	private OWLWorkspace workspace;	
	private OWLModelManager modelManager;
	
	private Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();
		workspace = editorKit.getWorkspace();	
		modelManager = editorKit.getOWLModelManager();
	}

	@Override
	public void dispose() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OWLReasoner reasoner = modelManager.getOWLReasonerManager().getCurrentReasoner();
		if (reasoner instanceof QuestOWL) {
			try {
				QuestOWL questReasoner = (QuestOWL) reasoner;
				boolean isConsistent = questReasoner.isQuestConsistent();
				log.debug("Checking for inconsistency returned: "+isConsistent);
				if (isConsistent) {
					JOptionPane.showMessageDialog(null, "Your ontology is consistent! Great job!");
				} else {
					JOptionPane.showMessageDialog(null, "Your ontology is not consistent. The axiom creating inconsistency is: \n"
							+questReasoner.getInconsistentAxiom().toString());
				}
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(null, "An error occured. For more info, see the logs.");
				log.error("Error during inconsistency checking. \n"+ex.getMessage()+"\n"+ex.getLocalizedMessage());
			}
		}
		else {
			JOptionPane.showMessageDialog(null, "You have to start ontop reasoner for this feature.");
		}
	}

}
