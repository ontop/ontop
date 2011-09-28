package it.unibz.krdb.obda.protege4.gui.action;

import it.unibz.krdb.obda.gui.swing.utils.OBDAProgessMonitor;
import it.unibz.krdb.obda.gui.swing.utils.OBDAProgressListener;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2IndividualIterator;
import it.unibz.krdb.obda.protege4.core.OBDAModelManager;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Action to create individuals into the currently open OWL Ontology using the
 * existing mappings from ALL datasources
 * 
 * @author Mariano Rodriguez Muro
 */
public class AboxMaterializationAction extends ProtegeAction {

	private static final long serialVersionUID = -1211395039869926309L;
	
	private MaterializeAction action = null;

	private Logger log = LoggerFactory.getLogger(AboxMaterializationAction.class);
	
	@Override
	public void initialise() throws Exception {
		// Does nothing!
	}

	@Override
	public void dispose() throws Exception {
		// Does nothing!
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (!(getEditorKit() instanceof OWLEditorKit)) {
			return;
		}

		int response = JOptionPane
				.showConfirmDialog(
						this.getEditorKit().getWorkspace(),
						"This will use the mappings of the OWL-OBDA model \n to create a set of 'individual' assertions as specified \n by the mappings. \n\n This operation can take a long time and can require a lot of memory \n if the volume data retrieved by the mappings is high.",
						"Confirm", JOptionPane.OK_CANCEL_OPTION);
		
		if (response == JOptionPane.CANCEL_OPTION) {
			return;
		}

		OWLEditorKit kit = (OWLEditorKit)getEditorKit();
		Container container = getWorkspace().getRootPane().getParent();
		OBDAModel obdaModel = ((OBDAModelManager) kit.get(OBDAModelImpl.class.getName())).getActiveOBDAModel();

		try {
			OWLModelManager modelManager = kit.getOWLModelManager();
			OWLAPI2IndividualIterator individuals = new OWLAPI2IndividualIterator(obdaModel);
			
			action = new MaterializeAction(modelManager, individuals);
			
			Thread th = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						OBDAProgessMonitor monitor = new OBDAProgessMonitor();
						CountDownLatch latch = new CountDownLatch(1);
						action.setCountdownLatch(latch);
						monitor.addProgressListener(action);
						monitor.start("Materializing Abox ....");
						action.run();
						latch.await();
						monitor.stop();
					} 
					catch (InterruptedException e) {
						log.error(e.getMessage(), e);
						JOptionPane.showMessageDialog(null, "ERROR: could not materialize ABox.");
					}
				}
			});
			th.start();
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			JOptionPane.showMessageDialog(container, "ERROR: could not create individuals. See the log for more informaiton.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class MaterializeAction implements OBDAProgressListener {

		private Thread thread = null;
		private CountDownLatch latch = null;
		
		private OWLOntology currentOntology = null;
		private OWLOntologyManager ontologyManager = null;
		private OWLAPI2IndividualIterator iterator = null;
		
		private boolean bCancel = false;

		public MaterializeAction(OWLModelManager modelManager, OWLAPI2IndividualIterator iterator) {
			currentOntology = modelManager.getActiveOntology();
			ontologyManager = modelManager.getOWLOntologyManager();			
			this.iterator = iterator;
		}

		public void setCountdownLatch(CountDownLatch cdl){
			latch = cdl;
		}
		
		public void run() {
			if (latch == null){
				try {
					throw new Exception("No CountDownLatch set");
				}
				catch (Exception e) {
					log.error(e.getMessage(), e);
					JOptionPane.showMessageDialog(null, "ERROR: could not materialize abox.");
					return;
				}
			}
			
			thread = new Thread() {
				public void run() {
					try {
						while(iterator.hasNext()) {
							ontologyManager.addAxiom(currentOntology, iterator.next());
						}
						
						latch.countDown();
						Container cont = AboxMaterializationAction.this.getWorkspace().getRootPane().getParent();
						if(!bCancel){
							JOptionPane.showMessageDialog(cont, "Task is completed", "Done", JOptionPane.INFORMATION_MESSAGE);
						}
					}
					catch (Exception e) {
						latch.countDown();
						log.error("Materialization of Abox failed", e);
					}
				}
			};
			thread.start();
		}

		@Override
		public void actionCanceled() {
			if (thread != null) {
				bCancel = true;
				latch.countDown();
				thread.interrupt();
			}
		}

	}

}
