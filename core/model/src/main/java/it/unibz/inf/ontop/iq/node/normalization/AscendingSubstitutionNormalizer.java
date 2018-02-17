package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.Optional;

public interface AscendingSubstitutionNormalizer {

    AscendingSubstitutionNormalization normalizeAscendingSubstitution(
            ImmutableSubstitution<ImmutableTerm> ascendingSubstitution,
            ImmutableSet<Variable> projectedVariables);


    interface AscendingSubstitutionNormalization {

        Optional<ConstructionNode> generateTopConstructionNode();

        IQTree updateChild(IQTree child);

        ImmutableSubstitution<ImmutableTerm> getAscendingSubstitution();
    }
}
