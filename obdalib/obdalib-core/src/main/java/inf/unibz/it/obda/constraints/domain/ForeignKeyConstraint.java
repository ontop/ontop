package inf.unibz.it.obda.constraints.domain;

import inf.unibz.it.obda.constraints.AbstractConstraintAssertion;
import inf.unibz.it.obda.domain.Query;

import java.util.List;

import org.obda.query.domain.Variable;

public abstract class ForeignKeyConstraint extends AbstractConstraintAssertion{

	public abstract Query getSourceQueryOne();
	public abstract Query getSourceQueryTwo();
	public abstract List<Variable> getVariablesOfQueryOne();
	public abstract List<Variable> getVariablesOfQueryTwo();
}
