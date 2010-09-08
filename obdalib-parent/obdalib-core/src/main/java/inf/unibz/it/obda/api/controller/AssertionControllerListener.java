package inf.unibz.it.obda.api.controller;

import inf.unibz.it.dl.assertion.Assertion;

public interface AssertionControllerListener<AssertionClass extends Assertion> {
	
	public void assertionAdded(AssertionClass assertion);
	
	public void assertionRemoved(AssertionClass assertion);
	
	public void assertionChanged(AssertionClass oldAssertion, AssertionClass newAssertion);
	
	public void assertionsCleared();
}
