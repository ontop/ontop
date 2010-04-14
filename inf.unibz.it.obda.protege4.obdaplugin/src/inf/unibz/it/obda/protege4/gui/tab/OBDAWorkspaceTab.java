package inf.unibz.it.obda.protege4.gui.tab;

import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;

public class OBDAWorkspaceTab extends OWLWorkspaceViewsTab {
    private static final long serialVersionUID = -4896884982262745722L;
//    
//    private OWLEntityDisplayProvider provider = new OWLEntityDisplayProvider() {
//        public boolean canDisplay(OWLEntity owlEntity) {
//            return false;
//        }
//
//        public JComponent getDisplayComponent() {
//            return OBDAWorkspaceTab.this;
//        }
//    };

    
//    private boolean canDisplay(OWLEntity owlEntity) {
//        // search the contained views to see if there is one that can show the entity
//        
//        for (View view : getViewsPane().getViews()){
//            ViewComponent vc = view.getViewComponent();
//            if (vc instanceof AbstractOWLSelectionViewComponent){
//                final AbstractOWLSelectionViewComponent owlEntityViewComponent = (AbstractOWLSelectionViewComponent)vc;
//                if (owlEntityViewComponent.canShowEntity(owlEntity)){
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    @Override
    public void initialise() {
        super.initialise();
//        ((OBDAPluginController)APIController.getController()).setEditorKit(getOWLEditorKit());
    }


    @Override
    public void dispose() {
//        getOWLEditorKit().getWorkspace().unregisterOWLEntityDisplayProvider(provider);
        super.dispose();
    }


}
