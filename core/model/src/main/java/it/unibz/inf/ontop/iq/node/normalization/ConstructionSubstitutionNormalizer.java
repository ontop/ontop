package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

public interface ConstructionSubstitutionNormalizer {

    ConstructionSubstitutionNormalization normalizeSubstitution(
            Substitution<? extends ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);


    interface ConstructionSubstitutionNormalization {

        IQTree updateChild(IQTree child, VariableGenerator variableGenerator);

        ImmutableExpression updateExpression(ImmutableExpression expression);

        Substitution<ImmutableTerm> getNormalizedSubstitution();
    }
}
