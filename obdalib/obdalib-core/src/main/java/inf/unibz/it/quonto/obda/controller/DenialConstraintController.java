package inf.unibz.it.quonto.obda.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.quonto.dl.assertion.DenialConstraint;

import java.util.Collection;

public class DenialConstraintController extends AssertionController<DenialConstraint> {

	public DenialConstraintController() {
		
	}
	
	@Override
	public AssertionController<DenialConstraint> getInstance() {
		return new DenialConstraintController();
	}

	public Collection<String> getAttributes() {
		return null;
	}

	public String getElementTag() {
		return "DenialConstraints";
	}

}
