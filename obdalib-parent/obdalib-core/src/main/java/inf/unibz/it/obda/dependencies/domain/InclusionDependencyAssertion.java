package inf.unibz.it.obda.dependencies.domain;

import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.ucq.domain.QueryTerm;

import java.util.List;


/**
 * Abstract class representing a inclusion dependency assertion. All
 * inclusion dependency assertions should implements this abstract class.
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 *
 *
 */
public abstract class InclusionDependencyAssertion extends AbstractDependencyAssertion {
	
	public abstract SourceQuery getSourceQueryOne();
	public abstract SourceQuery getSourceQueryTwo();
	public abstract List<QueryTerm> getTermsOfQueryOne();
	public abstract List<QueryTerm> getTermsOfQueryTwo();
}
