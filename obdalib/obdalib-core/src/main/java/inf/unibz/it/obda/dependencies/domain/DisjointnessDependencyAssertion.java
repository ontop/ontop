package inf.unibz.it.obda.dependencies.domain;

import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.domain.Query;

import java.util.List;

import org.obda.query.domain.Variable;

/**
 * Abstract class representing a disjointness dependency assertion. All
 * disjointness dependency assertions should implements this abstract class.
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 * @author Josef Hardi <josef.hardi@unibz.it>
 *		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */
public abstract class DisjointnessDependencyAssertion
		extends AbstractDependencyAssertion {
	public abstract Query getSourceQueryOne();
	public abstract Query getSourceQueryTwo();
	public abstract List<Variable> getVariablesOfQueryOne();
	public abstract List<Variable> getVariablesOfQueryTwo();
}
