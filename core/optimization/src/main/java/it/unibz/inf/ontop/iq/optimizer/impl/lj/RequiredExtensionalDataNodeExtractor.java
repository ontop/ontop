package it.unibz.inf.ontop.iq.optimizer.impl.lj;


import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.LeftJoinDecomposition;
import static it.unibz.inf.ontop.iq.impl.NaryIQTreeTools.InnerJoinDecomposition;


@Singleton
public class RequiredExtensionalDataNodeExtractor {

    @Inject
    protected RequiredExtensionalDataNodeExtractor() {
    }

    /**
     * Not required to be exhaustive
     *
     * Expects the tree to come from a normalized tree
     * (but won't fail if it is not the case)
     *
     */
    public Stream<ExtensionalDataNode> extractSomeRequiredNodesFromLeft(IQTree tree) {

        if (tree instanceof ExtensionalDataNode)
            return Stream.of((ExtensionalDataNode) tree);

        var join = InnerJoinDecomposition.of(tree);
        if (join.isPresent())
            return join.getChildren().stream()
                    .flatMap(this::extractSomeRequiredNodesFromLeft);

        var leftJoin = LeftJoinDecomposition.of(tree);
        if (leftJoin.isPresent())
            return extractSomeRequiredNodesFromLeft(leftJoin.leftChild());

        return extractOtherType(tree);
    }

    public Stream<ExtensionalDataNode> extractSomeRequiredNodesFromRight(IQTree tree) {

        if (tree instanceof ExtensionalDataNode)
            return Stream.of((ExtensionalDataNode) tree);

        var join = InnerJoinDecomposition.of(tree);
        if (join.isPresent())
            return join.getChildren().stream()
                    .flatMap(this::extractSomeRequiredNodesFromRight);

        /*
         * TODO: see how to safely extract data nodes in the fromRight case
         */
        // Usually at the top of the right child of a LJ, with a substitution with ground terms (normally provenance constants)
        var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
        if (construction.isPresent()
                && construction.getNode().getSubstitution()
                .rangeAllMatch(ImmutableTerm::isGround))
            return extractSomeRequiredNodesFromRight(construction.getChild());

        return extractOtherType(tree);
    }

    /**
     * By default, not digging into other kind of trees
     */
    protected Stream<ExtensionalDataNode> extractOtherType(IQTree tree) {
        return Stream.empty();
    }
}
