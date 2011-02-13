package org.obda.reformulation.protege4.action;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.obda.owlrefplatform.core.ABoxToDBDumper;
import org.obda.reformulation.protege4.configpanel.SelectDB;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class AboxToDBAction extends ProtegeAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8210706765886897292L;
	private SelectDB selectDialog = null;

	public void initialise() throws Exception {
		
	}

	public void dispose() throws Exception {
		
	}

	public void actionPerformed(ActionEvent e) {
		loadAboxToDB();
	}

	
	private void loadAboxToDB(){
		
		if(getEditorKit() instanceof OWLEditorKit && 
				getEditorKit().getModelManager() instanceof OWLModelManager) 
		{
			OWLEditorKit kit = (OWLEditorKit) this.getEditorKit();
			OWLModelManager mm = kit.getOWLModelManager();
			
			OWLOntologyManager owlOntManager = mm.getOWLOntologyManager();
			Set<OWLOntology> ontologies = owlOntManager.getOntologies();
			ABoxToDBDumper dump = ABoxToDBDumper.getInstance();
			selectDialog = new SelectDB(new JFrame(), false,dump.getController(), dump, ontologies);
			selectDialog.setLocationRelativeTo(kit.getWorkspace().getParent());
			Runnable showdialog = new Runnable() {
			     public void run() {
			    	 selectDialog.setVisible(true);
			     }
			 };
			 SwingUtilities.invokeLater(showdialog);
		}
		
	}
}
