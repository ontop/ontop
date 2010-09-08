package inf.unibz.it.quonto.obda.controller;

import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.quonto.dl.assertion.IDConstraint;
import inf.unibz.it.quonto.obda.controller.IDConstraintController;
import inf.unibz.it.quonto.obda.controller.IDConstraintController;

import java.util.Collection;

public class IDConstraintController extends AssertionController<IDConstraint> {

	public IDConstraintController() {

	}

	@Override
	public AssertionController<IDConstraint> getInstance() {
		return new IDConstraintController();
	}

	public Collection<String> getAttributes() {
		return null;
	}

	public String getElementTag() {
		return "IDConstraints";
	}

}
