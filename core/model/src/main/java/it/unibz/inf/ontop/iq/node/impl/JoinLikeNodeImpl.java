package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.JoinLikeNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    private final ImmutabilityTools immutabilityTools;

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition,
                               TermNullabilityEvaluator nullabilityEvaluator,
                               TermFactory termFactory,
                               TypeFactory typeFactory, DatalogTools datalogTools,
                               ExpressionEvaluator defaultExpressionEvaluator,
                               ImmutabilityTools immutabilityTools) {
        super(optionalJoinCondition, nullabilityEvaluator, termFactory, typeFactory, datalogTools,
                defaultExpressionEvaluator);
        this.immutabilityTools = immutabilityTools;
    }

    /**
     * TODO: explain
     */
    protected Optional<ExpressionEvaluator.EvaluationResult> computeAndEvaluateNewCondition(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            Optional<ImmutableExpression> optionalNewEqualities) {

        Optional<ImmutableExpression> updatedExistingCondition = getOptionalFilterCondition()
                .map(substitution::applyToBooleanExpression);

        Optional<ImmutableExpression> newCondition = immutabilityTools.foldBooleanExpressions(
                Stream.concat(
                    Stream.of(updatedExistingCondition),
                    Stream.of(optionalNewEqualities))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .flatMap(e -> e.flattenAND().stream()));

        return newCondition
                .map(cond -> createExpressionEvaluator()
                        .evaluateExpression(cond));
    }

    protected static ImmutableSet<Variable> union(ImmutableSet<Variable> set1, ImmutableSet<Variable> set2) {
        return Stream.concat(
                set1.stream(),
                set2.stream())
                .collect(ImmutableCollectors.toSet());
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        ImmutableMultiset<Variable> childrenVariableBag = query.getChildren(this).stream()
                .flatMap(c -> query.getVariables(c).stream())
                .collect(ImmutableCollectors.toMultiset());

        Stream<Variable> cooccuringVariableStream = childrenVariableBag.entrySet().stream()
                .filter(e -> e.getCount() > 1)
                .map(Multiset.Entry::getElement);

        return Stream.concat(cooccuringVariableStream, getLocallyRequiredVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    protected ImmutabilityTools getImmutabilityTools() {
        return immutabilityTools;
    }
}
