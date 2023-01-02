package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLExpression;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;

import java.util.Optional;

public class SQLFlattenExpressionImpl implements SQLFlattenExpression {

    private final SQLExpression subExpression;
    private final Variable flattenedVar;
    private final Variable outputVar;
    private final Optional<Variable> indexVar;
    private final DBTermType flattenedType;

    @AssistedInject
    private SQLFlattenExpressionImpl(@Assisted SQLExpression subExpression,
                                     @Assisted("flattenedVar") Variable flattenedVar,
                                     @Assisted("outputVar") Variable outputVar,
                                     @Assisted Optional<Variable> indexVar,
                                     @Assisted DBTermType flattenedType) {
        this.subExpression = subExpression;
        this.flattenedVar = flattenedVar;
        this.outputVar = outputVar;
        this.indexVar = indexVar;
        this.flattenedType = flattenedType;
    }

    @Override
    public SQLExpression getSubExpression() {
        return subExpression;
    }


    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public Variable getFlattenedVar() {
        return flattenedVar;
    }

    public Variable getOutputVar() {
        return outputVar;
    }

    public Optional<Variable> getIndexVar() {
        return indexVar;
    }

    @Override
    public DBTermType getFlattenedType() {
        return flattenedType;
    }
}
