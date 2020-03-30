package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.Attribute;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ExtensionalDataNode;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.LeftJoinNode;
import it.unibz.inf.ontop.iq.transform.impl.DefaultIdentityIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TODO: find a better name
 *
 * Tries to insert a fresh non-nullable variable in a sparse data node of the tree.
 *
 * If created, such variable will be non-nullable at the tree level.
 *
 * Used during the left join normalization for creating a provenance variable without
 * inserting a construction node.
 *
 */
@Singleton
public class FreshVariableInserter {

    private final CoreSingletons coreSingletons;

    @Inject
    protected FreshVariableInserter(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    public IQTree tryToInsertFreshVariable(IQTree tree, VariableGenerator variableGenerator) {
        return new FreshVariableTransformer(coreSingletons, variableGenerator)
                .transform(tree);
    }

    protected static class FreshVariableTransformer extends DefaultIdentityIQTreeVisitingTransformer {

        private final IntermediateQueryFactory iqFactory;
        private final VariableGenerator variableGenerator;

        public FreshVariableTransformer(CoreSingletons coreSingletons, VariableGenerator variableGenerator) {
            this.iqFactory = coreSingletons.getIQFactory();
            this.variableGenerator = variableGenerator;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            List<Attribute> attributes = dataNode.getRelationDefinition().getAttributes();
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> argumentMap = dataNode.getArgumentMap();
            Optional<Integer> optionalIndex = IntStream.range(0, attributes.size())
                    .filter(i -> !argumentMap.containsKey(i))
                    .filter(i -> !attributes.get(i).canNull())
                    .boxed()
                    .findFirst();

            return optionalIndex
                    // Creates a fresh variable and inserts it into the map
                    .map(i -> Stream.concat(
                            argumentMap.entrySet().stream(),
                            Stream.of(Maps.immutableEntry(i, variableGenerator.generateNewVariable("prov"))))
                            .collect(ImmutableCollectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue)))
                    .map(m -> iqFactory.createExtensionalDataNode(dataNode.getRelationDefinition(), m))
                    .orElse(dataNode);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree newLeftChild = leftChild.acceptTransformer(this);
            return (newLeftChild.getVariables().equals(leftChild.getVariables()))
                    // No variable inserted
                    ? tree
                    : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, rightChild);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            for (int i=0; i < children.size(); i++) {
                IQTree child = children.get(i);
                IQTree newChild = child.acceptTransformer(this);
                if (!newChild.getVariables().equals(child.getVariables())) {
                    int indexToReplace = i;
                    ImmutableList<IQTree> newChildren = IntStream.range(0, children.size())
                            .mapToObj(j -> j == indexToReplace ? newChild : children.get(j))
                            .collect(ImmutableCollectors.toList());

                    return iqFactory.createNaryIQTree(rootNode, newChildren);
                }
            }
            // No fresh variable inserted
            return tree;
        }
    }
}
