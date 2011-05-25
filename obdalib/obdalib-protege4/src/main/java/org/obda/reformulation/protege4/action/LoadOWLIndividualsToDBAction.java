package org.obda.reformulation.protege4.action;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;
import inf.unibz.it.utils.swing.OBDAProgessMonitor;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.obda.owlrefplatform.core.abox.ABoxToDBDumper;
import org.obda.reformulation.protege4.configpanel.SelectDB;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoadOWLIndividualsToDBAction extends ProtegeAction {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8210706765886897292L;
	private SelectDB			selectDialog		= null;
	
	Logger log = LoggerFactory.getLogger(LoadOWLIndividualsToDBAction.class);

	public void initialise() throws Exception {

	}

	public void dispose() throws Exception {

	}

	public void actionPerformed(ActionEvent e) {
		loadAboxToDB();
	}

	private void loadAboxToDB() {

		if (!(getEditorKit() instanceof OWLEditorKit && getEditorKit().getModelManager() instanceof OWLModelManager)) {
			return;
		}
		OWLEditorKit kit = (OWLEditorKit) this.getEditorKit();
		OWLModelManager mm = kit.getOWLModelManager();

		OWLOntologyManager owlOntManager = mm.getOWLOntologyManager();
		final Set<OWLOntology> ontologies = owlOntManager.getOntologies();

		Runnable showdialog = new Runnable() {
			public void run() {
				OBDAPluginController controller = getEditorKit().get(APIController.class.getName());
				selectDialog = new SelectDB(new JFrame(), true, controller.getOBDAManager());
				selectDialog.setLocationRelativeTo(getEditorKit().getWorkspace().getParent());
				selectDialog.setVisible(true);
				String selectedsource = selectDialog.getSelectedSource();
				final DataSource source = controller.getOBDAManager().getDatasourcesController().getDataSource(URI.create(selectedsource));
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						OBDAProgessMonitor monitor = new OBDAProgessMonitor();
						monitor.start();
						try {
							ABoxToDBDumper dump = new ABoxToDBDumper(source);
							monitor.addProgressListener(dump);
							dump.materialize(ontologies, selectDialog.isOverrideSelected());
							monitor.stop();
							if (!monitor.isCanceled()) {
								JOptionPane.showMessageDialog(null, "Dump successful!", "", JOptionPane.PLAIN_MESSAGE);
							}
						} catch (Exception e) {
							monitor.stop();
							log.error(e.getMessage());
							log.debug(e.getMessage(), e);
							JOptionPane.showMessageDialog(null, "Error during the dumping. " + e.getMessage(), "FAILURE",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				th.start();

			}
		};
		SwingUtilities.invokeLater(showdialog);
	}

}
