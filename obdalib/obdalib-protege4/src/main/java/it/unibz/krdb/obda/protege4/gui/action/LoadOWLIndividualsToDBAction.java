package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.RDBMSDirectDataRepositoryManager;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2ABoxIterator;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2VocabularyExtractor;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;
import it.unibz.krdb.obda.protege4.dialogs.SelectDB;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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

	Logger						log					= LoggerFactory.getLogger(LoadOWLIndividualsToDBAction.class);

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
				OBDAModelManager controller = getEditorKit().get(OBDAModelImpl.class.getName());
				selectDialog = new SelectDB(new JFrame(), true, controller.getActiveOBDAModel());
				selectDialog.setLocationRelativeTo(getEditorKit().getWorkspace().getParent());
				selectDialog.setVisible(true);
				String selectedsource = selectDialog.getSelectedSource();
				final OBDADataSource source = controller.getActiveOBDAModel().getSource(URI.create(selectedsource));
				Thread th = new Thread(new Runnable() {
					@Override
					public void run() {
						OBDAProgessMonitor monitor = new OBDAProgessMonitor();
						monitor.start();
						try {

							OWLAPI2VocabularyExtractor vext = new OWLAPI2VocabularyExtractor();
							RDBMSDirectDataRepositoryManager dbmanager = new RDBMSDirectDataRepositoryManager(source,
									vext.getVocabulary(ontologies));
							monitor.addProgressListener(dbmanager);

							dbmanager.createDBSchema(true);
							dbmanager.insertMetadata();
							OWLAPI2ABoxIterator aboxiterator = new OWLAPI2ABoxIterator(ontologies);
							dbmanager.insertData(aboxiterator);

							monitor.stop();
							if (!monitor.isCanceled()) {
								JOptionPane.showMessageDialog(null, "Dump successful.", "", JOptionPane.PLAIN_MESSAGE);
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
