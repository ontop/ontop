package it.unibz.inf.ontop.spec.mapping.pp.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;

public class ConstructionNodeFlattenerImpl implements ConstructionNodeFlattener {

    private final SubstitutionFactory substitutionFactory;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private ConstructionNodeFlattenerImpl(SubstitutionFactory substitutionFactory, IntermediateQueryFactory iqFactory) {
        this.substitutionFactory = substitutionFactory;
        this.iqFactory = iqFactory;
    }


    @Override
    public MappingAssertion transform(MappingAssertion assertion) {
        IQTree topChild = assertion.getTopChild();
        if (topChild.getRootNode() instanceof ConstructionNode) {
            ImmutableSubstitution<ImmutableTerm> childSubstitution = ((ConstructionNode) topChild.getRootNode()).getSubstitution();
            ImmutableSubstitution<ImmutableTerm> composedSubstitution = substitutionFactory.getSubstitution(
                    assertion.getTopSubstitution().getImmutableMap().entrySet().stream()
                            .collect(ImmutableCollectors.toMap(Map.Entry::getKey, e -> childSubstitution.apply(e.getValue()))));

            return assertion.copyOf(iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(assertion.getProjectedVariables(), composedSubstitution),
                    ((UnaryIQTree)topChild).getChild()), iqFactory);
        }
        return assertion;
    }
}
