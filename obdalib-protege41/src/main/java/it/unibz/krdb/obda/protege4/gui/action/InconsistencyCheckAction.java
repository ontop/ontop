/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.protege4.gui.action;

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
				if (isConsistent) {
					JOptionPane.showMessageDialog(workspace, "Your ontology is consistent! Great job!");
				} else {
					JOptionPane.showMessageDialog(workspace, "Your ontology is not consistent. The axiom creating inconsistency is: "
							+questReasoner.getInconsistentAxiom().toString());
				}
				
			}catch(Exception ex){
				JOptionPane.showMessageDialog(workspace, "An error occured. For more info, see the logs.");
				log.error("Error during inconsistency checking. \n"+ex.getMessage()+"\n"+ex.getLocalizedMessage());
			}
		}
	}

}
