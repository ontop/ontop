package it.unibz.inf.ontop.iq.tools.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.AbstractRelationDefinition;
import it.unibz.inf.ontop.dbschema.impl.SQLStandardQuotedIDFactory;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
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
import java.util.stream.IntStream;

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

    public static ImmutableList<ExtensionalDataNode> getExtensionalDataNodes(IQTree tree,
                                                                                       CoreSingletons coreSingletons) {
        QueryNode node = tree.getRootNode();
        if (node instanceof FilterNode) {
            return getExtensionalDataNodes(tree.getChildren().get(0), coreSingletons);
        }
        else if (node instanceof ExtensionalDataNode) {
            return ImmutableList.of((ExtensionalDataNode)tree);
        }
        else if (node instanceof TrueNode) {
            return ImmutableList.of();
        }
        else if (node instanceof ValuesNode) {
            return ImmutableList.of(convertIntoExtensionalDataNode((ValuesNode) node, coreSingletons));
        }
        else if (node instanceof InnerJoinNode) {
            if (tree.getChildren().stream().anyMatch(c -> !(c.getRootNode() instanceof ExtensionalDataNode)))
                throw new MinorOntopInternalBugException("Unexpected IQ structure for InnerJoin " + tree);

            return tree.getChildren().stream()
                    .map(n -> (ExtensionalDataNode)n)
                    .collect(ImmutableCollectors.toList());
        }
        throw new MinorOntopInternalBugException("Unexpected IQ structure " + tree);
    }

    public static ImmutableSet<ImmutableExpression> getFilterExpressions(IQTree tree) {
        QueryNode node = tree.getRootNode();
        if (node instanceof FilterNode) {
            return ((FilterNode)tree.getRootNode()).getOptionalFilterCondition().get().flattenAND()
                    .collect(ImmutableCollectors.toSet());
        }
        else if (node instanceof ExtensionalDataNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof TrueNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof ValuesNode) {
            return ImmutableSet.of();
        }
        else if (node instanceof InnerJoinNode) {
            return ((InnerJoinNode)tree.getRootNode()).getOptionalFilterCondition()
                    .map(e -> e.flattenAND()
                            .collect(ImmutableCollectors.toSet()))
                    .orElseGet(ImmutableSet::of);
        }
        throw new MinorOntopInternalBugException("Use getExtensionalDataNodes first to check whether it's a CQ " + tree);
    }

    /**
     * Values nodes are represented as extensional data nodes with special atom predicates
     */
    private static ExtensionalDataNode convertIntoExtensionalDataNode(ValuesNode node, CoreSingletons coreSingletons) {

        ImmutableList<Variable> variables = node.getOrderedVariables();

        ImmutableMap<Integer, Variable> argumentMap = IntStream.range(0, variables.size())
                .boxed()
                .collect(ImmutableCollectors.toMap(
                        i -> i,
                        variables::get));

        return coreSingletons.getIQFactory().createExtensionalDataNode(
                new ValuesRelationDefinition(node,
                        coreSingletons.getTypeFactory().getDBTypeFactory().getAbstractRootDBType(),
                        new SQLStandardQuotedIDFactory()),
                argumentMap);
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
