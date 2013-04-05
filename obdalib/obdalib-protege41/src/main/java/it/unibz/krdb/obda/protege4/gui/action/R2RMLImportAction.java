package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.io.R2RMLReader;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2RMLImportAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;

	private OWLEditorKit editorKit = null;
	private OBDAModel obdaModel = null;
	
	private Logger log = LoggerFactory.getLogger(R2RMLImportAction.class);
	
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
		
		String message = "The imported mappings will be appended to the existing data source. Continue?";
		int response = JOptionPane.showConfirmDialog(workspace, message, "Confirmation", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION) {
			
			final JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(workspace);
			try {

				File file = (fc.getSelectedFile());
				if (file != null)
				{
				R2RMLReader reader = new R2RMLReader(file);

				URI sourceID = obdaModel.getSources().get(0).getSourceID();

				obdaModel.addMappings(sourceID, reader.readMappings());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
        

	}

}
