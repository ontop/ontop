package it.unibz.inf.ontop.generation.algebra;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.QuotedID;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

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
                                                       @Assisted("sortConditions") ImmutableList<SQLOrderComparator> sortConditions);

    SQLSerializedQuery createSQLSerializedQuery(String sqlString, ImmutableMap<Variable, QuotedID> columnNames);

    SQLTable createSQLTable(RelationDefinition relationDefinition, ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap);

    SQLInnerJoinExpression createSQLInnerJoinExpression(@Assisted("leftExpression") SQLExpression left, @Assisted("rightExpression") SQLExpression right, Optional<ImmutableExpression> joinCondition);

    SQLLeftJoinExpression createSQLLeftJoinExpression(@Assisted("leftExpression") SQLExpression leftExpression, @Assisted("rightExpression") SQLExpression rightExpression, Optional<ImmutableExpression> joinCondition);

    SQLNaryJoinExpression createSQLNaryJoinExpression(ImmutableList<SQLExpression> joinedExpressions);

    SQLUnionExpression createSQLUnionExpression(ImmutableList<SQLExpression> subExpressions, ImmutableSet<Variable> projectedVariables);

    SQLOneTupleDummyQueryExpression createSQLOneTupleDummyQueryExpression();

    SQLValuesExpression createSQLValues(@Assisted("orderedVariables") ImmutableList<Variable> orderedVariables,
                                        @Assisted("values") ImmutableList<ImmutableList<Constant>> values);

    SQLOrderComparator createSQLOrderComparator(NonConstantTerm term, boolean isAscending);
}
