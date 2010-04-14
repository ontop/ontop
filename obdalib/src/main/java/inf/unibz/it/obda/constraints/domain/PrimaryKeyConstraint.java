package inf.unibz.it.obda.constraints.domain;

import java.util.List;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.ucq.domain.QueryTerm;

public abstract class PrimaryKeyConstraint extends AbstractConstraintAssertion {

	public abstract SourceQuery getSourceQuery();
	public abstract List<QueryTerm> getTerms();
}
