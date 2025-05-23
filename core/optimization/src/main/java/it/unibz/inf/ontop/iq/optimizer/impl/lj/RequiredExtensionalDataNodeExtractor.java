package it.unibz.inf.ontop.iq.optimizer.impl.lj;


import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.IQTreeTools.NaryIQTreeDecomposition;
import static it.unibz.inf.ontop.iq.impl.BinaryNonCommutativeIQTreeTools.LeftJoinDecomposition;


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
    public Stream<ExtensionalDataNode> extractSomeRequiredNodes(IQTree tree, boolean fromLeft) {

        if (tree instanceof ExtensionalDataNode)
            return Stream.of((ExtensionalDataNode) tree);

        var join = NaryIQTreeDecomposition.of(tree, InnerJoinNode.class);
        if (join.isPresent())
            return join.getChildren().stream()
                    .flatMap(t -> extractSomeRequiredNodes(t, fromLeft));

        /*
         * TODO: see how to safely extract data nodes in the fromRight case
         */
        var leftJoin = LeftJoinDecomposition.of(tree);
        if (fromLeft && leftJoin.isPresent())
            return extractSomeRequiredNodes(leftJoin.leftChild(), true);

        // Usually at the top of the right child of a LJ, with a substitution with ground terms (normally provenance constants)
        if (!fromLeft) {
            var construction = UnaryIQTreeDecomposition.of(tree, ConstructionNode.class);
            if (construction.isPresent()
                && construction.getNode().getSubstitution()
                    .rangeAllMatch(ImmutableTerm::isGround))
                return extractSomeRequiredNodes(construction.getChild(), false);
        }
        return extractOtherType(tree);
    }

    /**
     * By default, not digging into other kind of trees
     */
    protected Stream<ExtensionalDataNode> extractOtherType(IQTree tree) {
        return Stream.empty();
    }
}
