package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public interface ConstructionSubstitutionNormalizer {

    ConstructionSubstitutionNormalization normalizeSubstitution(
            ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);


    interface ConstructionSubstitutionNormalization {

        Optional<ConstructionNode> generateTopConstructionNode();

        IQTree updateChild(IQTree child);

        ImmutableExpression updateExpression(ImmutableExpression expression);

        ImmutableSubstitution<ImmutableTerm> getNormalizedSubstitution();
    }
}
