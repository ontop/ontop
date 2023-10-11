package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class IQ2CQ {



    public static IQTree toIQTree(ImmutableList<? extends IQTree> extensionalNodes, Optional<ImmutableExpression> joiningConditions,
                                  CoreSingletons coreSingletons) {
        ImmutableList<IQTree> children = extensionalNodes.stream()
                .map(n -> (n instanceof ExtensionalDataNode)
                        ? convertDataNodeToIQ((ExtensionalDataNode) n, coreSingletons)
                        : n)
                .collect(ImmutableCollectors.toList());

        IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

        switch (children.size()) {
            case 0:
                return iqFactory.createTrueNode();
            case 1:
                return (joiningConditions.isPresent()
                        ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(joiningConditions.get()), children.get(0))
                        : children.get(0));
            default:
                return iqFactory.createNaryIQTree(
                        iqFactory.createInnerJoinNode(joiningConditions),
                        children.stream().collect(ImmutableCollectors.toList()));
        }
    }

    private static IQTree convertDataNodeToIQ(ExtensionalDataNode dataNode, CoreSingletons coreSingletons) {
        /*
         * "Fake" extensional data node converted back into a Values node
         */
        if (dataNode.getRelationDefinition() instanceof ValuesRelationDefinition) {
            ValuesNode originalValuesNode = ((ValuesRelationDefinition) dataNode.getRelationDefinition()).getValuesNode();

            VariableGenerator variableGenerator = coreSingletons.getCoreUtilsFactory().createVariableGenerator(
                    Sets.union(dataNode.getKnownVariables(), originalValuesNode.getKnownVariables()));

            SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

            InjectiveSubstitution<Variable> freshRenaming = originalValuesNode.getOrderedVariables().stream()
                    .collect(substitutionFactory.toFreshRenamingSubstitution(variableGenerator));

            ValuesNode freshValuesNode = originalValuesNode.applyFreshRenaming(freshRenaming);
            ImmutableList<Variable> freshVariables = freshValuesNode.getOrderedVariables();

            Substitution<? extends VariableOrGroundTerm> descendingSubstitution = dataNode.getArgumentMap().entrySet().stream()
                    .collect(substitutionFactory.toSubstitution(
                            e -> freshVariables.get(e.getKey()),
                            Map.Entry::getValue));

            IQTree newValuesNode = freshValuesNode.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution, variableGenerator);

            IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

            return dataNode.getVariables().containsAll(newValuesNode.getVariables())
                    ? newValuesNode
                    : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(dataNode.getVariables()), newValuesNode);
        }
        else
            return dataNode;
    }

    /**
     * Temporary (should disappear when converting back the CQ into IQ
     */
    private static class ValuesRelationDefinition extends AbstractRelationDefinition {

        private final ValuesNode valuesNode;

        private ValuesRelationDefinition(ValuesNode valuesNode, DBTermType rootType, QuotedIDFactory idFactory) {
            super("values_" + Hashing.sha256()
                            .hashString(valuesNode.toString(), StandardCharsets.UTF_8)
                            .toString(),
                    extractAttributes(valuesNode, rootType, idFactory));
            this.valuesNode = valuesNode;
        }

        private static RelationDefinition.AttributeListBuilder extractAttributes(ValuesNode valuesNode, DBTermType rootType, QuotedIDFactory idFactory) {
            RelationDefinition.AttributeListBuilder builder = AbstractRelationDefinition.attributeListBuilder();
            VariableNullability variableNullability = valuesNode.getVariableNullability();

            valuesNode.getOrderedVariables()
                    .forEach(v -> builder.addAttribute(idFactory.createAttributeID(v.getName()), rootType,
                            variableNullability.isPossiblyNullable(v)));
            return builder;
        }

        public ValuesNode getValuesNode() {
            return valuesNode;
        }

        @Override
        public ImmutableList<UniqueConstraint> getUniqueConstraints() {
            return ImmutableList.of();
        }

        @Override
        public ImmutableList<FunctionalDependency> getOtherFunctionalDependencies() {
            return ImmutableList.of();
        }

        @Override
        public ImmutableList<ForeignKeyConstraint> getForeignKeys() {
            return ImmutableList.of();
        }
    }
}
