package inf.unibz.it.obda.protege4.constraints;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSPrimaryKeyConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSPrimaryKeyConstraint;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSPrimaryKeyConstraintFactoryPlugin  extends AssertionControllerFactoryPluginInstance {

	@Override
	public Class<?> getAssertionClass() {
		return RDBMSPrimaryKeyConstraint.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		return new RDBMSPrimaryKeyConstraintController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		return null; // TODO Constraint codec: Fix this!
//		return new RDBMSPrimaryKeyConstraintXMLCodec();
	}

	@Override
	public boolean triggersOntologyChanged() {
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
