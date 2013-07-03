package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.owlapi3.bootstrapping.DirectMappingBootstrapper;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.event.EventType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapAction extends ProtegeAction {

	private OWLEditorKit editorKit = null;
	private OWLWorkspace workspace;	
	private OWLModelManager owlManager;
	private OBDAModelManager modelManager;
	private DirectMappingBootstrapper dm = null;
	private String baseUri = "";
	private OWLOntology currentOnto;
	private OBDAModel currentModel;
	private OBDADataSource currentSource;
	
	private Logger log = LoggerFactory.getLogger(BootstrapAction.class);
	
	@Override
	public void initialise() throws Exception {
		editorKit = (OWLEditorKit)getEditorKit();	
		workspace = editorKit.getWorkspace();	
		owlManager = editorKit.getOWLModelManager();
		modelManager = ((OBDAModelManager)editorKit.get(OBDAModelImpl.class.getName()));
	}

	@Override
	public void dispose() throws Exception {
		//NOP
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		 currentOnto = owlManager.getActiveOntology();
		 currentModel = modelManager.getActiveOBDAModel();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("Choose a datasource to bootstrap: "), BorderLayout.NORTH);
		List<String> options = new ArrayList<String>();
		for (OBDADataSource source : currentModel.getSources())
			options.add(source.getSourceID().toString());
		JComboBox combo = new JComboBox(options.toArray());
		panel.add(combo);
		int res = JOptionPane.showOptionDialog(workspace, panel, "Bootstrapping", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (res == JOptionPane.OK_OPTION) {
			int index = combo.getSelectedIndex();

			currentSource = currentModel.getSources().get(index);
			if (currentSource != null) {
				final String baseUri = JOptionPane.showInputDialog(workspace, "Ontology base URI: ", "Input Base URI", JOptionPane.PLAIN_MESSAGE).trim();
				if (baseUri != null && !baseUri.isEmpty()) {
				this.baseUri = baseUri;
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							OBDAProgessMonitor monitor = new OBDAProgessMonitor(
									"Bootstrapping ontology and mappings from datasource...");
							BootstrapperThread t = new BootstrapperThread();
							monitor.addProgressListener(t);
							monitor.start();
							t.run(baseUri, currentOnto, currentModel, currentSource);
							currentModel = dm.getModel();
							currentOnto = dm.getOntology();
							currentModel.fireSourceParametersUpdated();
							monitor.stop();

						} catch (Exception e) {
							log.error(e.getMessage(), e);
							JOptionPane
									.showMessageDialog(null,
											"Error occured during bootstrapping data source.");
						}
					}
				});
				th.start();
				JOptionPane.showMessageDialog(this.workspace,
						"Task is completed.", "Done",
						JOptionPane.INFORMATION_MESSAGE);
			}
			}
		}
	}

	private class BootstrapperThread implements OBDAProgressListener {

		
		@Override
		public void actionCanceled() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		public void run(String baseUri, OWLOntology currentOnto, OBDAModel currentModel, OBDADataSource currentSource) throws Exception
		{
			dm = new DirectMappingBootstrapper(baseUri, currentOnto,
					currentModel, currentSource);
		}
		
	}
	
}
