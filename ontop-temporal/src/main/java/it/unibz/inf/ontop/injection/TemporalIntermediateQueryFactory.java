package it.unibz.inf.ontop.injection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.temporal.iq.TemporalIntermediateQueryBuilder;
import it.unibz.inf.ontop.temporal.iq.node.*;
import it.unibz.inf.ontop.temporal.model.TemporalRange;

import java.util.Optional;

/**
 * Factory following the Guice AssistedInject pattern.
 *
 * See https://github.com/google/guice/wiki/AssistedInject.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public interface TemporalIntermediateQueryFactory extends IntermediateQueryFactory {

    TemporalIntermediateQueryBuilder createTemporalIQBuilder(DBMetadata metadata, ExecutorRegistry executorRegistry);
    TemporalJoinNode createTemporalJoinNode();
    TemporalJoinNode createTemporalJoinNode(ImmutableExpression joiningCondition);
    TemporalJoinNode createTemporalJoinNode(Optional<ImmutableExpression> joiningCondition);

    BoxMinusNode createBoxMinusNode(TemporalRange temporalRange);

    BoxPlusNode createBoxPlusNode(TemporalRange temporalRange);

    DiamondMinusNode createDiamondMinusNode(TemporalRange temporalRange);

    DiamondPlusNode createDiamondPlusNode(TemporalRange temporalRange);

    SinceNode createSinceNode(TemporalRange temporalRange);

    UntilNode createUntilNode(TemporalRange temporalRange);

    TemporalCoalesceNode createTemporalCoalesceNode(ImmutableList<NonGroundTerm> projectedVariables);

    //TemporalRange createTemporalRange(Boolean beginInclusive, Boolean endInclusive, Duration begin, Duration end);
}
