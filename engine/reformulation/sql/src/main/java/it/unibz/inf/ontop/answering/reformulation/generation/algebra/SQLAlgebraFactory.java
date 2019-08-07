package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.iq.node.OrderByNode;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

/**
 * TODO: complete
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface SQLAlgebraFactory {

    SelectFromWhereWithModifiers createSelectFromWhere(ImmutableSortedSet<Variable> projectedVariables,
                                                       ImmutableSubstitution<? extends ImmutableTerm> substitution,
                                                       @Assisted("fromExpression") SQLExpression fromExpression,
                                                       @Assisted("whereExpression") Optional<ImmutableExpression> whereExpression,
                                                       @Assisted("groupBy") ImmutableSet<Variable> groupByVariables,
                                                       boolean isDistinct,
                                                       @Assisted("limit") Optional<Long> limit,
                                                       @Assisted("offset") Optional<Long> offset,
                                                       @Assisted("sortConditions") ImmutableList<OrderByNode.OrderComparator> sortConditions);

    SQLSerializedQuery createSQLSerializedQuery(String sqlString, ImmutableMap<Variable, String> columnNames);

    SQLTable createSQLTable(DataAtom<RelationPredicate> atom);

    SQLInnerJoinExpression createSQLInnerJoinExpression(@Assisted("leftExpression") SQLExpression left, @Assisted("rightExpression") SQLExpression right);

    SQLLeftJoinExpression createSQLLeftJoinExpression(@Assisted("leftExpression") SQLExpression leftExpression, @Assisted("rightExpression") SQLExpression rightExpression, Optional<ImmutableExpression> joinCondition);

    SQLNaryJoinExpression createSQLNaryJoinExpression(ImmutableList<SQLExpression> joinedExpressions);

    SQLUnionExpression createSQLUnionExpression(ImmutableList<SQLExpression> subExpressions, ImmutableSet<Variable> projectedVariables);

    SQLOneTupleDummyQueryExpression createSQLOneTupleDummyQueryExpression();
}
