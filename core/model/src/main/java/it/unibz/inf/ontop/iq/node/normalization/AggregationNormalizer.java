package it.unibz.inf.ontop.iq.node.normalization;

import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.AggregationNode;
import it.unibz.inf.ontop.iq.node.DistinctNode;
import it.unibz.inf.ontop.utils.VariableGenerator;

public interface AggregationNormalizer {

    IQTree normalizeForOptimization(AggregationNode aggregationNode, IQTree child, VariableGenerator variableGenerator,
                                    IQProperties currentIQProperties);
}
