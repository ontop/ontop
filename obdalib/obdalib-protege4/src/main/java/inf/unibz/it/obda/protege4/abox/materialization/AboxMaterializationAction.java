package inf.unibz.it.obda.protege4.abox.materialization;

import inf.unibz.it.obda.owlapi.abox.materialization.AboxMaterializer;
import inf.unibz.it.utils.swing.OBDAProgessMonitor;
import inf.unibz.it.utils.swing.OBDAProgressListener;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owl.model.OWLIndividualAxiom;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class AboxMaterializationAction extends ProtegeAction {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -1211395039869926309L;

	@Override
	public void initialise() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (!(getEditorKit() instanceof OWLEditorKit) || !(getEditorKit().getModelManager() instanceof OWLModelManager))
			return;

		int response = JOptionPane
				.showConfirmDialog(
						this.getEditorKit().getWorkspace(),
						"This will use the mappings of the OWL-OBDA model \n to create a set of 'individual' assertions as specified \n by the mappings. \n\n This operation can take a long time and can require a lot of memory \n if the volume data retrieved by the mappings is high.",
						"Confirm", JOptionPane.OK_CANCEL_OPTION);
		if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION || response == JOptionPane.NO_OPTION)
			return;

		
		
		OWLEditorKit kit = (OWLEditorKit) this.getEditorKit();
		OWLModelManager mm = kit.getOWLModelManager();
		Container cont = this.getWorkspace().getRootPane().getParent();
		try {
			OWLOntologyManager owlOntManager = mm.getOWLOntologyManager();
			OWLOntology owl_ont = mm.getActiveOntology();
			String file = owlOntManager.getPhysicalURIForOntology(owl_ont).getPath();
			AboxMaterializer mat = new AboxMaterializer();
			
			OWLModelManager dlModelManager = (OWLModelManager) getEditorKit().getModelManager();
			OWLOntologyManager dlOntManager = dlModelManager.getOWLOntologyManager();

			OBDAProgessMonitor monitor = new OBDAProgessMonitor();
			CountDownLatch latch = new CountDownLatch(1);
			MaterializeAction action = new MaterializeAction(mat, file, owl_ont, owlOntManager, latch);
			monitor.addProgressListener(action);
			monitor.start();
			action.run();
			latch.await();
			monitor.stop();
			Set<OWLIndividualAxiom> individuals = action.getResult();
			
			if(individuals != null){
				dlOntManager.addAxioms(owl_ont, individuals);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(cont, "ERROR: could not materialize abox.");
			e.printStackTrace();
		}

	}
	
	private class MaterializeAction implements OBDAProgressListener{

		private Thread thread = null;
		private Set<OWLIndividualAxiom> result = null;
		private CountDownLatch latch = null;
		private String file = null;
		private AboxMaterializer mat = null;
		private OWLOntology owl_ont = null;
		private OWLOntologyManager man = null;
		
		public MaterializeAction(AboxMaterializer mat,String file,OWLOntology owl_ont,OWLOntologyManager man,CountDownLatch latch){
			this.file = file;
			this.mat = mat;
			this.owl_ont = owl_ont;
			this.man = man;
			this.latch = latch;
		}
		
		public Set<OWLIndividualAxiom> getResult(){
			return result;
		}
		
		public void run(){
			thread = new Thread(){
				public void run(){
					try {
						result = mat.materializeAbox(file, owl_ont, man);
						latch.countDown();
					} catch (Exception e) {
						latch.countDown();
						JOptionPane.showMessageDialog(null,"ERROR: could not materialize abox.");
					}
				}
			};
			thread.start();
		}
		
		@Override
		public void actionCanceled() {
			if(thread != null){
				latch.countDown();
				thread.interrupt();
			}			
		}
		
	}

}
