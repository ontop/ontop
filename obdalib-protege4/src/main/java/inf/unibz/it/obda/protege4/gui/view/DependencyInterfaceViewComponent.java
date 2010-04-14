package inf.unibz.it.obda.protege4.gui.view;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.dependencies.panel.DependencyTabPane;
import inf.unibz.it.obda.gui.swing.dependencies.panel.Dependency_SelectMappingPane;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import org.protege.editor.owl.ui.inference.ReasonerProgressUI;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DependencyInterfaceViewComponent extends AbstractOWLViewComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2606287000824211618L;
	private DependencyTabPane tab= null;
	private Dependency_SelectMappingPane tree= null;
	
	@Override
	protected void disposeOWLView() {
		
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
		
		setLayout(new BorderLayout());
		JSplitPane split_right_horizontal = new javax.swing.JSplitPane();
		tab = new DependencyTabPane(apic);
		tree = new Dependency_SelectMappingPane(apic);
		
		split_right_horizontal.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		split_right_horizontal.setResizeWeight(0.5);
		split_right_horizontal.setOneTouchExpandable(true);
		split_right_horizontal.setTopComponent(tab);
		split_right_horizontal.setBottomComponent(tree);
		
		add(split_right_horizontal, BorderLayout.CENTER);
	}

}
