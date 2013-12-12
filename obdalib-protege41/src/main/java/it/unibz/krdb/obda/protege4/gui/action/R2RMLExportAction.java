package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.sesame.r2rml.R2RMLWriter;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2RMLExportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	
	private Logger log = LoggerFactory.getLogger(R2RMLExportAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();		
		obdaModel = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		final OWLWorkspace workspace = editorKit.getWorkspace();	
		  URI sourceID = obdaModel.getSources().get(0).getSourceID();
		  
		  final JFileChooser fc = new JFileChooser();
		  fc.setSelectedFile(new File(sourceID+"-mappings.ttl"));
          fc.showSaveDialog(workspace);
          File file = fc.getSelectedFile();
          
    	  
		R2RMLWriter writer = new R2RMLWriter(obdaModel, sourceID);
		writer.write(file);

	}
}
