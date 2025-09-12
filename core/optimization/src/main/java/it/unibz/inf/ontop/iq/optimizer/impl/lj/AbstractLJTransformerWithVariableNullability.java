package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.node.impl.JoinOrFilterVariableNullabilityTools;
import it.unibz.inf.ontop.iq.node.normalization.impl.RightProvenanceNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class AbstractLJTransformerWithVariableNullability extends  AbstractLJTransformer {
    private final Supplier<VariableNullability> variableNullabilitySupplier;
    // LAZY
    private VariableNullability variableNullability;

    protected final RightProvenanceNormalizer rightProvenanceNormalizer;
    protected final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    protected final TermFactory termFactory;

    protected AbstractLJTransformerWithVariableNullability(IQTreeTransformer transformBySearchingFromScratch,
                                                           Supplier<VariableNullability> variableNullabilitySupplier,
                                                           VariableGenerator variableGenerator,
                                                           RightProvenanceNormalizer rightProvenanceNormalizer,
                                                           JoinOrFilterVariableNullabilityTools variableNullabilityTools,
                                                           CoreSingletons coreSingletons) {
        super(coreSingletons.getIQFactory(), variableGenerator, transformBySearchingFromScratch);

        this.termFactory = coreSingletons.getTermFactory();

        this.variableNullabilitySupplier = variableNullabilitySupplier;
        this.rightProvenanceNormalizer = rightProvenanceNormalizer;
        this.variableNullabilityTools = variableNullabilityTools;
    }

    protected synchronized VariableNullability getInheritedVariableNullability() {
        if (variableNullability == null)
            variableNullability = variableNullabilitySupplier.get();

        return variableNullability;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected VariableNullability computeRightChildVariableNullability(IQTree rightChild, Optional<ImmutableExpression> ljCondition) {
        VariableNullability bottomUpNullability = rightChild.getVariableNullability();

        VariableNullability nullabilityWithLJCondition = getVariableNullability(ljCondition, bottomUpNullability, rightChild.getVariables());

        ImmutableSet<Variable> nullableVariablesAfterLJCondition = nullabilityWithLJCondition.getNullableVariables();

        if (nullableVariablesAfterLJCondition.isEmpty())
            return nullabilityWithLJCondition;

        VariableNullability inheritedNullability = getInheritedVariableNullability();

        // Non-nullability information coming from the ancestors
        Optional<ImmutableExpression> additionalFilter = termFactory.getConjunction(nullableVariablesAfterLJCondition.stream()
                .filter(v -> !inheritedNullability.isPossiblyNullable(v))
                .map(termFactory::getDBIsNotNull));

        return getVariableNullability(additionalFilter, nullabilityWithLJCondition, rightChild.getVariables());
    }

    protected Supplier<VariableNullability> computeChildVariableNullabilityFromConstructionParent(IQTree tree,
                                                                                                  ConstructionNode rootNode, IQTree child) {
        var childVariables = child.getVariables();
        var inheritedVariableNullability = getInheritedVariableNullability();
        var bottomUpVariableNullability = tree.getVariableNullability();

        var isNotNullConditions = termFactory.getConjunction(childVariables.stream()
                .filter(v -> !rootNode.getSubstitution().isDefining(v))
                .filter(bottomUpVariableNullability::isPossiblyNullable)
                .filter(v -> !inheritedVariableNullability.isPossiblyNullable(v))
                .map(termFactory::getDBIsNotNull));

        return () -> getVariableNullability(isNotNullConditions, child.getVariableNullability(), childVariables);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private VariableNullability getVariableNullability(Optional<ImmutableExpression> condition, VariableNullability variableNullability, ImmutableSet<Variable> variables) {
        return condition
                .map(c -> variableNullabilityTools.updateWithFilter(
                        c,
                        variableNullability.getNullableGroups(),
                        variables))
                .orElse(variableNullability);
    }
}
