package inf.unibz.it.quonto.obda.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.quonto.dl.assertion.EQLConstraint;

import java.util.Collection;


public class EQLConstraintController extends AssertionController<EQLConstraint> {

	public EQLConstraintController() {
		
	}
	
	@Override
	public AssertionController<EQLConstraint> getInstance() {
		return new EQLConstraintController();
	}

	public Collection<String> getAttributes() {
		return null;
	}

	public String getElementTag() {
		return "EQLConstraints";
	}


}
