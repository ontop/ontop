package inf.unibz.it.obda.dependencies;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import inf.unibz.it.dl.assertion.Assertion;
import inf.unibz.it.obda.api.controller.AssertionController;
import inf.unibz.it.obda.api.controller.DatasourcesControllerListener;

/**.
 * All dependency controller should implement this abstract class. It adds
 * the possibility to assign assertions to different databases
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 *
 * @param <AssertionClass>
 */

public abstract class AbstractDependencyAssertionController<AssertionClass extends Assertion> extends
		AssertionController<AssertionClass> implements DatasourcesControllerListener{

	/**
	 * Returns a set of Assertions assigned to the currently selected 
	 * data source
	 * 
	 * @return
	 */
	public abstract HashSet<AssertionClass> getDependenciesForCurrentDataSource();
	
	public abstract HashSet<AssertionClass> getAssertionsForDataSource(URI uri);
	
}
