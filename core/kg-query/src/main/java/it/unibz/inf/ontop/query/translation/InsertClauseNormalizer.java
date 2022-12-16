package it.unibz.inf.ontop.query.translation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.IntensionalDataNode;
import it.unibz.inf.ontop.model.term.BNode;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

/**
 * Replaces blank nodes in the data node by fresh variables.
 * These fresh variables are defined by combining variables provided by the whereTree.
 */
public interface InsertClauseNormalizer {

    InsertClauseNormalizer.Result normalize(ImmutableSet<IntensionalDataNode> dataNodes, IQTree whereTree);

    interface Result {
        ImmutableMap<BNode, Variable> getBNodeVariableMap();
        Optional<ConstructionNode> getConstructionNode();
    }
}
