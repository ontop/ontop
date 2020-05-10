package it.unibz.inf.ontop.spec.mapping.sqlparser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.sqlparser.exception.IllegalJoinException;

import java.util.function.Function;

public interface RAEntity<T> {
    T withAlias(RelationID aliasId);

    T crossJoin(T right) throws IllegalJoinException;
    T joinUsing(T right, ImmutableSet<QuotedID> using) throws IllegalJoinException;
    T joinOn(T right, Function<ImmutableMap<QualifiedAttributeID, ImmutableTerm>, ImmutableList<ImmutableExpression>> getAtomOnExpression) throws IllegalJoinException;

    ImmutableSet<QuotedID> getSharedAttributeNames(T right); // for natural join
}
