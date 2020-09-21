package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.generation.algebra.SQLValuesExpression;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.Variable;

public class SQLValuesExpressionImpl implements SQLValuesExpression {

    private final ImmutableList<Variable> orderedVariables;
    private final ImmutableList<ImmutableList<Constant>> values;

    @Inject
    private SQLValuesExpressionImpl(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                                    @Assisted("values") ImmutableList<ImmutableList<Constant>> values) {

        this.orderedVariables = orderedVariables;
        this.values = values;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ImmutableList<Variable> getOrderedVariables() {
        return orderedVariables;
    }

    @Override
    public ImmutableList<ImmutableList<Constant>> getValues() {
        return values;
    }
}
