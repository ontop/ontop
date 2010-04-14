package inf.unibz.it.obda.protege4.plugin;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.ExtensionInstantiator;
import org.protege.editor.core.plugin.JPFUtil;

public class AssertionControllerFactoryPluginImpl implements AssertionControllerFactoryPlugin {

    private IExtension iExtension;

//    private OWLEditorKit editorKit;

    public AssertionControllerFactoryPluginImpl(IExtension iExtension) {
        this.iExtension = iExtension;
//        this.editorKit = editorKit;
    }


    public String getId() {
        return ID;
    }


    public String getDocumentation() {
        return JPFUtil.getDocumentation(iExtension);
    }


    public AssertionControllerFactoryPluginInstance newInstance() throws ClassNotFoundException, IllegalAccessException,
                                                         InstantiationException {
        ExtensionInstantiator<AssertionControllerFactoryPluginInstance> instantiator = new ExtensionInstantiator<AssertionControllerFactoryPluginInstance>(iExtension);
        AssertionControllerFactoryPluginInstance instance = instantiator.instantiate();
//        instance.setup(editorKit);
        return instance;
    }
}
