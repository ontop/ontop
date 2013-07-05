package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi3.bootstrapping.DirectMappingBootstrapper;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLOntology;
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
		editorKit = (OWLEditorKit) getEditorKit();
		workspace = editorKit.getWorkspace();
		owlManager = editorKit.getOWLModelManager();
		modelManager = ((OBDAModelManager) editorKit.get(OBDAModelImpl.class
				.getName()));
	}

	@Override
	public void dispose() throws Exception {
		// NOP
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		currentOnto = owlManager.getActiveOntology();
		currentModel = modelManager.getActiveOBDAModel();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JLabel dsource = new JLabel("Choose a datasource to bootstrap: ");
		dsource.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(dsource);
		List<String> options = new ArrayList<String>();
		for (OBDADataSource source : currentModel.getSources())
			options.add(source.getSourceID().toString());
		JComboBox combo = new JComboBox(options.toArray());
		combo.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(combo);
		Dimension minsize = new Dimension(10, 10);
		panel.add(new Box.Filler(minsize, minsize, minsize));
		JLabel ouri = new JLabel(
				"Base URI - the prefix to be used for all generated classes and properties: ");
		ouri.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(ouri);
		JTextField base_uri = new JTextField();
		base_uri.setText(currentModel.getPrefixManager().getDefaultPrefix()
				.replace("#", "/"));
		base_uri.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(base_uri);
		int res = JOptionPane.showOptionDialog(workspace, panel,
				"Bootstrapping", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (res == JOptionPane.OK_OPTION) {
			int index = combo.getSelectedIndex();
			currentSource = currentModel.getSources().get(index);
			if (currentSource != null) {
				this.baseUri = base_uri.getText().trim();
				if (baseUri.contains("#")) {
					JOptionPane.showMessageDialog(workspace,
							"Base Uri cannot contain the character '#'");
					throw new RuntimeException("Base URI " + baseUri
							+ " contains '#' character!");
				} else {
					Thread th = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								OBDAProgessMonitor monitor = new OBDAProgessMonitor(
										"Bootstrapping ontology and mappings...");
								BootstrapperThread t = new BootstrapperThread();
								monitor.addProgressListener(t);
								monitor.start();
								t.run(baseUri, currentOnto, currentModel,
										currentSource);
								currentModel.fireSourceParametersUpdated();
								monitor.stop();
								JOptionPane.showMessageDialog(workspace,
										"Task is completed.", "Done",
										JOptionPane.INFORMATION_MESSAGE);
							} catch (Exception e) {
								log.error(e.getMessage(), e);
								JOptionPane
										.showMessageDialog(null,
												"Error occured during bootstrapping data source.");
							}
						}
					});
					th.start();
				}
			}
		}
	}

	private class BootstrapperThread implements OBDAProgressListener {

		@Override
		public void actionCanceled() throws Exception {
			// TODO Auto-generated method stub

		}

		public void run(String baseUri, OWLOntology currentOnto,
				OBDAModel currentModel, OBDADataSource currentSource)
				throws Exception {
			dm = new DirectMappingBootstrapper(baseUri, currentOnto,
					currentModel, currentSource);
		}

	}

}
