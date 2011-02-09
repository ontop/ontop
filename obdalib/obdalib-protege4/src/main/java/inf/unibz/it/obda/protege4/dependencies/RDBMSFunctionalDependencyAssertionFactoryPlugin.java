package inf.unibz.it.obda.protege4.dependencies;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSFunctionalDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSFunctionalDependency;
//import inf.unibz.it.obda.dl.codec.dependencies.xml.RDBMSFunctionalDependencyXMLCodec;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSFunctionalDependencyAssertionFactoryPlugin extends AssertionControllerFactoryPluginInstance {

	@Override
	public Class<?> getAssertionClass() {
		
		return RDBMSFunctionalDependency.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		
		return new RDBMSFunctionalDependencyController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		return null; // TODO Dependency codec: fix this!
//		return new RDBMSFunctionalDependencyXMLCodec();
	}

	@Override
	public boolean triggersOntologyChanged() {
		
		return true;
	}

	public void initialise() throws Exception {
	}

	public void dispose() throws Exception {
	}

}
