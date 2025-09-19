package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.node.SliceNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface SliceNormalizer {
    IQTree normalizeForOptimization(SliceNode sliceNode, IQTree initialChild,
                                    VariableGenerator variableGenerator, IQTreeCache treeCache);
}
