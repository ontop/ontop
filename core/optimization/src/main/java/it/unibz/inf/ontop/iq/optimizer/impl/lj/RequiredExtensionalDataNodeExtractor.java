package it.unibz.inf.ontop.iq.optimizer.impl.lj;


import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.term.ImmutableTerm;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.Stream;

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
        QueryNode root = tree.getRootNode();

        if (tree instanceof ExtensionalDataNode)
            return Stream.of((ExtensionalDataNode) tree);

        if (root instanceof InnerJoinNode)
            return tree.getChildren().stream()
                    .flatMap(tree1 -> extractSomeRequiredNodes(tree1, fromLeft));

        /*
         * TODO: see how to safely extract data nodes in the fromRight case
         */
        if (fromLeft && (root instanceof LeftJoinNode))
            return extractSomeRequiredNodes(((BinaryNonCommutativeIQTree) tree).getLeftChild(), true);

        // Usually at the top of the right child of a LJ, with a substitution with ground terms (normally provenance constants)
        if ((!fromLeft) && (root instanceof ConstructionNode)
                && ((ConstructionNode) root).getSubstitution().getImmutableMap().values().stream()
                .allMatch(ImmutableTerm::isGround))
            return extractSomeRequiredNodes(((UnaryIQTree) tree).getChild(), false);

        return extractOtherType(tree);
    }

    /**
     * By default, not digging into other kind of trees
     */
    protected Stream<ExtensionalDataNode> extractOtherType(IQTree tree) {
        return Stream.empty();
    }


}
