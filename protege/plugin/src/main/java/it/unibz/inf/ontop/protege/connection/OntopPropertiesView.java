package it.unibz.inf.ontop.protege.connection;

import it.unibz.inf.ontop.protege.core.OBDAEditorKitSynchronizerPlugin;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;

import java.awt.*;

public class OntopPropertiesView extends AbstractOWLViewComponent {

    private OBDAModelManager obdaModelManager;
    private OntopPropertiesPanel panel;

    @Override
    protected void initialiseOWLView()  {
        OWLEditorKit editorKit = getOWLEditorKit();
        obdaModelManager = OBDAEditorKitSynchronizerPlugin.getOBDAModelManager(editorKit);

        panel = new OntopPropertiesPanel(editorKit);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);

        obdaModelManager.addListener(panel);
    }

    @Override
    protected void disposeOWLView() {
        obdaModelManager.removeListener(panel);
    }
}
