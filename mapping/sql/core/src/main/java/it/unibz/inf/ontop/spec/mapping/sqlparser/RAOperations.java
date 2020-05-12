package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;

import java.util.function.Function;

public interface RAOperations<T> {

    T create();

    T withAlias(T rae, RelationID aliasId);

    T crossJoin(T left, T right) throws IllegalJoinException;
    T joinUsing(T left, T right, ImmutableSet<QuotedID> using) throws IllegalJoinException;
    T joinOn(T left, T right, Function<RAExpressionAttributes, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException;

    default T naturalJoin(T left, T right) throws IllegalJoinException {
        return joinUsing(left, right, getSharedAttributeNames(left, right));
    }

    ImmutableSet<QuotedID> getSharedAttributeNames(T left, T right);

}
