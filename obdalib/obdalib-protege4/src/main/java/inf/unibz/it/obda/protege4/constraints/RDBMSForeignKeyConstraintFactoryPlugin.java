package inf.unibz.it.obda.protege4.constraints;

import inf.unibz.it.dl.codec.xml.AssertionXMLCodec;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.constraints.controller.RDBMSForeignKeyConstraintController;
import inf.unibz.it.obda.constraints.domain.imp.RDBMSForeignKeyConstraint;
//import inf.unibz.it.obda.dl.codec.constraints.xml.RDBMSForeignKeyConstraintXMLCodec;
import inf.unibz.it.obda.protege4.plugin.AssertionControllerFactoryPluginInstance;

public class RDBMSForeignKeyConstraintFactoryPlugin  extends AssertionControllerFactoryPluginInstance {

	@Override
	public Class<?> getAssertionClass() {
		
		return RDBMSForeignKeyConstraint.class;
	}

	@Override
	public AssertionController<?> getControllerInstance() {
		
		return new RDBMSForeignKeyConstraintController();
	}

	@Override
	public AssertionXMLCodec<?> getXMLCodec() {
		return null; // TODO Constraint codec: Fix this!
//		return new RDBMSForeignKeyConstraintXMLCodec();
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
