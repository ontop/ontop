package inf.unibz.it.obda.constraints;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;

import java.net.URI;
import java.util.HashSet;

public abstract class AbstractConstraintAssertionController<AssertionClass extends Assertion> extends
		AssertionController<AssertionClass> implements DatasourcesControllerListener{

	public AbstractConstraintAssertionController() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns a set of Assertions assigned to the currently selected 
	 * data source
	 * 
	 * @return
	 */
	public abstract HashSet<AssertionClass> getDependenciesForCurrentDataSource();
	
	public abstract HashSet<AssertionClass> getAssertionsForDataSource(URI uri);
	
}
