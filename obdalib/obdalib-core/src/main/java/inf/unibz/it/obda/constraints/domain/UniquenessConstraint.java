package inf.unibz.it.obda.constraints.domain;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.ucq.domain.QueryTerm;

import java.util.List;

public abstract class UniquenessConstraint extends AbstractConstraintAssertion{

	public abstract SourceQuery getSourceQuery();
	public abstract List<QueryTerm> getTerms();
}
