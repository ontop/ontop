package inf.unibz.it.obda.constraints.domain;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;

import java.util.List;

import org.obda.query.domain.Query;
import org.obda.query.domain.Variable;

public abstract class UniquenessConstraint extends AbstractConstraintAssertion{

	public abstract Query getSourceQuery();
	public abstract List<Variable> getVariables();
}
