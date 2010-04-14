package inf.unibz.it.obda.protege4.constraints;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSUniquenessConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSUniquenessConstraint;
import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSUniquenessConstraintXMLCodec;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSUniquenessConstraintFactoryPlugin  extends AssertionControllerFactoryPluginInstance {

	@Override
	public Class<?> getAssertionClass() {
		
		return RDBMSUniquenessConstraint.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		
		return new RDBMSUniquenessConstraintController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		
		return new RDBMSUniquenessConstraintXMLCodec();
	}

	@Override
	public boolean triggersOntologyChanged() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void initialise() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
