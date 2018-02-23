package it.unibz.inf.ontop.iq.optimizer;

import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.proposal.ConstructionNodeCleaningProposal;
import it.unibz.inf.ontop.iq.proposal.impl.ConstructionNodeCleaningProposalImpl;
import it.unibz.inf.ontop.iq.optimizer.impl.NodeCentricDepthFirstOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;


/**
 * Gets rid of unnecessary ConstructionNodes.
 * <p>
 * The algorithm searches the query q depth-first.
 * <p>
 * When a ConstructionNode is c_1 encountered,
 * find the highest value i such that:
 * .c_1, .., c_n is a chain composed of ConstructionNodes only,
 * with c1 as root,
 * c2 as child of c_1,
 * c3 child of c_2,
 * etc., and
 * .c_2, .., c_i have empty substitutions
 * <p>
 * Note that n = 1 may hold.
 * <p>
 * Then proceed as follows:
 * .combine all query modifiers found in c_1 to c_n,
 * and assign the combined modifiers to c_1
 * .After this,
 * let c' = c_2 if c_1 has a nonempty substitution or query modifiers,
 * and c' = c_1 otherwise.
 * Then replace in q the subtree rooted in c' by the subtree rooted in the child of c_n.
 * <p>
 *
 * UPDATE: Old-style query modifier handling removed BUT NOT REPLACED
 * TODO: mark it as deprecated?
 *
 */
public class ConstructionNodeCleaner extends NodeCentricDepthFirstOptimizer<ConstructionNodeCleaningProposal> {

    private static final Logger log = LoggerFactory.getLogger(ConstructionNodeCleaner.class);

    public ConstructionNodeCleaner() {
        super(false);
    }

    @Override
    public IntermediateQuery optimize(IntermediateQuery query) throws EmptyQueryException {
        IntermediateQuery optimizedQuery = super.optimize(query);
        log.trace("New query after construction node cleaning: \n" + optimizedQuery.toString());
        return optimizedQuery;
    }

    @Override
    protected Optional<ConstructionNodeCleaningProposal> evaluateNode(QueryNode node, IntermediateQuery query) {
        if (node instanceof ConstructionNode) {
            ConstructionNode castNode = (ConstructionNode) node;
            return makeProposal(
                    query,
                    castNode,
                    castNode,
                    query.getFirstChild(castNode)
            );
        }
        return Optional.empty();
    }

    private Optional<ConstructionNodeCleaningProposal> makeProposal(IntermediateQuery query,
                                                                    ConstructionNode constructionNodeChainRoot,
                                                                    ConstructionNode currentParentNode,
                                                                    Optional<QueryNode> currentChildNode) {

        boolean deleteConstructionNodeChain =
                constructionNodeChainRoot.getSubstitution().isEmpty() &&
                !constructionNodeChainRoot.equals(query.getRootNode());

        /* special case of a non-deletable unary chain */
        if (currentParentNode.equals(constructionNodeChainRoot) && !deleteConstructionNodeChain) {
            return Optional.empty();
        }

        return Optional.of(
                new ConstructionNodeCleaningProposalImpl(
                        constructionNodeChainRoot,
                        currentChildNode.isPresent() ?
                                currentChildNode.get() :
                                currentParentNode,
                        deleteConstructionNodeChain
                ));
    }
}
