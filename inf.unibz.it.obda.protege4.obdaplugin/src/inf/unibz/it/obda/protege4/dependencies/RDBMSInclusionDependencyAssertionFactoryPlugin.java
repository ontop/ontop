package inf.unibz.it.obda.protege4.dependencies;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSInclusionDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSInclusionDependency;
import inf.unibz.it.obda.dl.codec.dependencies.xml.RDBMSInclusionDependencyXMLCodec;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSInclusionDependencyAssertionFactoryPlugin extends AssertionControllerFactoryPluginInstance {

	@Override
	public Class<?> getAssertionClass() {
		
		return RDBMSInclusionDependency.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		
		return new RDBMSInclusionDependencyController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		
		return new RDBMSInclusionDependencyXMLCodec();
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
