package inf.unibz.it.obda.dependencies.domain;

import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;

import java.util.List;

import org.obda.query.domain.Query;
import org.obda.query.domain.Term;

/**
 * Abstract class representing a functional dependency assertion. All
 * functional dependency assertions should implements this abstract class.
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
* @author Josef Hardi <josef.hardi@unibz.it>
 *		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */

public abstract class FunctionalDependencyAssertion
		extends AbstractDependencyAssertion {
	public abstract Query getSourceQueryOne();
	public abstract Query getSourceQueryTwo();
	public abstract List<Term> getTermsOfQueryOne();
	public abstract List<Term> getTermsOfQueryTwo();
}
