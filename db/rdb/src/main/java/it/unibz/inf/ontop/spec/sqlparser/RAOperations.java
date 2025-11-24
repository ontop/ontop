package it.unibz.inf.ontop.spec.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.spec.sqlparser.exception.IllegalJoinException;

import java.util.Optional;
import java.util.function.Function;

public interface RAOperations<T> {

    /**
     * Creates a T for an empty (true) node
     * @return
     */

    T create();

    /**
     * Creates a T for a table.
     * Each table attribute A can be referenced by TID.A and TO.A,
     * where TID is any of the known table IDs and TO is the table-only name.
     * Each table attribute A occurs unambiguously in the table.
     * @param relation
     * @param variables
     * @return
     */

    T create(NamedRelationDefinition relation, ImmutableList<Variable> variables);

    T createWithoutName(RelationDefinition relation, ImmutableList<Variable> variables);

    T withAlias(T rae, RelationID aliasId);

    T crossJoin(T left, T right) throws IllegalJoinException;
    T joinUsing(T left, T right, ImmutableSet<QuotedID> using) throws IllegalJoinException;
    T joinOn(T left, T right, Function<RAExpressionAttributes, Optional<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException;

    default T naturalJoin(T left, T right) throws IllegalJoinException {
        return joinUsing(left, right, getSharedAttributeNames(left, right));
    }

    ImmutableSet<QuotedID> getSharedAttributeNames(T left, T right);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    T filter(T rae, Optional<ImmutableExpression> filter);
}
