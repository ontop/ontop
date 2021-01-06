package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;

import java.util.function.Function;

public interface RAOperations<T> {

    T create();
    T create(NamedRelationDefinition relation, ImmutableList<Variable> variables);

    T withAlias(T rae, RelationID aliasId);

    T crossJoin(T left, T right) throws IllegalJoinException;
    T joinUsing(T left, T right, ImmutableSet<QuotedID> using) throws IllegalJoinException;
    T joinOn(T left, T right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException;

    default T naturalJoin(T left, T right) throws IllegalJoinException {
        return joinUsing(left, right, getSharedAttributeNames(left, right));
    }

    ImmutableSet<QuotedID> getSharedAttributeNames(T left, T right);

    T filter(T rae, ImmutableList<ImmutableExpression> filter);
}
