package it.unibz.inf.ontop.pivotalrepr.impl;

import java.util.Optional;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.owlrefplatform.core.unfolding.ExpressionEvaluator;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.JoinLikeNode;

import static it.unibz.inf.ontop.model.impl.ImmutabilityTools.foldBooleanExpressions;

public abstract class JoinLikeNodeImpl extends JoinOrFilterNodeImpl implements JoinLikeNode {

    private static final OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();

    protected JoinLikeNodeImpl(Optional<ImmutableExpression> optionalJoinCondition) {
        super(optionalJoinCondition);
    }

    /**
     * TODO: explain
     */
    protected Optional<ExpressionEvaluator.Evaluation> computeAndEvaluateNewCondition(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {

        Optional<ImmutableExpression> formerCondition = getOptionalFilterCondition();

        return formerCondition
                .map(substitution::applyToBooleanExpression)
                .map(cond -> new ExpressionEvaluator(query.getMetadata().getUriTemplateMatcher())
                        .evaluateExpression(cond));
    }

}
