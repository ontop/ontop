package inf.unibz.it.obda.constraints.domain;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.dependencies.AbstractDependencyAssertion;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.ucq.domain.QueryTerm;

import java.util.List;

public abstract class ForeignKeyConstraint extends AbstractConstraintAssertion{

	public abstract SourceQuery getSourceQueryOne();
	public abstract SourceQuery getSourceQueryTwo();
	public abstract List<QueryTerm> getTermsOfQueryOne();
	public abstract List<QueryTerm> getTermsOfQueryTwo();
}
