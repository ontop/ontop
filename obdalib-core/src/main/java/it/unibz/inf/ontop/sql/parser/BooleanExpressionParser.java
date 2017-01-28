package it.unibz.inf.ontop.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.Term;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.sql.QualifiedAttributeID;
import it.unibz.inf.ontop.sql.QuotedIDFactory;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryRuntimeException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;

/**
 * Created by roman on 02/12/2016.
 */
public class BooleanExpressionParser implements java.util.function.Function<ImmutableMap<QualifiedAttributeID, Variable>, ImmutableList<Function>> {

    private final QuotedIDFactory idfac;
    private final Expression root;

    public   BooleanExpressionParser(QuotedIDFactory idfac, Expression expression) {
        this.idfac = idfac;
        this.root = expression;
    }

    @Override
    public ImmutableList<Function> apply(ImmutableMap<QualifiedAttributeID, Variable> attributes) {

        Expression current = root;

        if (current instanceof AndExpression) {
            ImmutableList.Builder<Function> builder = ImmutableList.builder();
            do {
                AndExpression and = (AndExpression) current;
                // for a sequence of AND operations, JSQLParser makes the right argument simple
                builder.add(getFunction(and.getRightExpression(), attributes));
                // and the left argument complex (nested AND)
                current = and.getLeftExpression();
            } while (current instanceof AndExpression);

            builder.add(getFunction(current, attributes));

            // restore the original order
            return builder.build().reverse();
        }
        return ImmutableList.of(getFunction(current, attributes));
    }

    private Function getFunction(Expression expression, ImmutableMap<QualifiedAttributeID, Variable> attributes) {
        ExpressionParser parser = new ExpressionParser(idfac, expression);
        Term t = parser.apply(attributes);
        if (t instanceof Function)
            return (Function)t;

        throw new UnsupportedSelectQueryRuntimeException("Unexpected conversion to Boolean", expression);
    }


}
