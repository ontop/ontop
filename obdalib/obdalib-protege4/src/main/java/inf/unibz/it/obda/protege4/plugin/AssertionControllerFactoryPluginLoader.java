package inf.unibz.it.obda.protege4.plugin;

import org.eclipse.core.runtime.IExtension;
import org.protege.editor.core.plugin.AbstractPluginLoader;
import org.protege.editor.core.plugin.DefaultPluginExtensionMatcher;
import org.protege.editor.core.plugin.PluginExtensionMatcher;



public class AssertionControllerFactoryPluginLoader extends AbstractPluginLoader<AssertionControllerFactoryPlugin> {

//    private OWLEditorKit editorKit;

    public AssertionControllerFactoryPluginLoader() {
        super("inf.unibz.it.obda.protege4.obdaplugin", AssertionControllerFactoryPlugin.ID);
//        this.editorKit = editorKit;
    }


    protected PluginExtensionMatcher getExtensionMatcher() {
        return new DefaultPluginExtensionMatcher();
    }


    protected AssertionControllerFactoryPlugin createInstance(IExtension extension) {
        return new AssertionControllerFactoryPluginImpl(extension);
    }
    
    
}
