package inf.unibz.it.obda.protege4.dependencies;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.dependencies.controller.RDBMSDisjointnessDependencyController;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.dl.codec.dependencies.xml.RDBMSDisjointnessDependencyXMLCodec;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSDisjointnessDependencyFactoryPlugin extends AssertionControllerFactoryPluginInstance {
	
	@Override
	public Class<?> getAssertionClass() {
		
		return RDBMSDisjointnessDependency.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		
		return new RDBMSDisjointnessDependencyController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		
		return new RDBMSDisjointnessDependencyXMLCodec();
	}

	@Override
	public boolean triggersOntologyChanged() {
		
		return true;
	}

	public void initialise() throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void dispose() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
