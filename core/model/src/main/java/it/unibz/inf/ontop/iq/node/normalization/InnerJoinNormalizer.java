package it.unibz.inf.ontop.iq.node.normalization;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface InnerJoinNormalizer {

    IQTree normalizeForOptimization(InnerJoinNode innerJoinNode, ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                                    IQTreeCache treeCache);
}
