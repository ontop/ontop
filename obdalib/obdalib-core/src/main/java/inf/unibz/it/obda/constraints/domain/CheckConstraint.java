package inf.unibz.it.obda.constraints.domain;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.ucq.typing.CheckOperationTerm;

import java.util.List;

import org.obda.query.domain.Query;

public abstract class CheckConstraint extends AbstractConstraintAssertion{

	public abstract Query getSourceQueryOne();
	public abstract List<CheckOperationTerm> getChecks();
}
