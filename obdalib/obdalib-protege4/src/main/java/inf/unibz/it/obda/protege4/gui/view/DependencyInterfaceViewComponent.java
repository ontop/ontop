package inf.unibz.it.obda.protege4.gui.view;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.gui.swing.dependencies.panel.DependencyTabPane;
import inf.unibz.it.obda.gui.swing.dependencies.panel.Dependency_SelectMappingPane;
import inf.unibz.it.obda.gui.swing.preferences.OBDAPreferences;
import inf.unibz.it.obda.protege4.core.OBDAPluginController;

import java.awt.BorderLayout;

import javax.swing.JSplitPane;

import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

public class DependencyInterfaceViewComponent extends AbstractOWLViewComponent {

	private DependencyTabPane tab = null;
	private Dependency_SelectMappingPane tree = null;
	
	private static final Logger log = 
	    Logger.getLogger(DependencyInterfaceViewComponent.class);
	
	@Override
	protected void disposeOWLView() {
		// Do nothing.
	}

	@Override
	protected void initialiseOWLView() throws Exception {
		
		OBDAPluginController apic = getOWLEditorKit().get(APIController.class.getName());
		
		setLayout(new BorderLayout());
				
		OBDAPreferences preference = (OBDAPreferences)
			getOWLEditorKit().get(OBDAPreferences.class.getName());
	
		tab = new DependencyTabPane(apic, preference);
		tree = new Dependency_SelectMappingPane(apic, preference);
		
		JSplitPane split_right_horizontal = new javax.swing.JSplitPane();
		split_right_horizontal.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		split_right_horizontal.setTopComponent(tab);
		split_right_horizontal.setBottomComponent(tree);
		
		add(split_right_horizontal, BorderLayout.CENTER);
		
    log.info("Dependency interface view initialized");
	}
}
