package it.unibz.inf.ontop.pivotalrepr.unfolding;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.IntermediateQuery;
import it.unibz.inf.ontop.pivotalrepr.QueryNode;
import it.unibz.inf.ontop.pivotalrepr.unfolding.impl.ProjectedVariableExtractorImpl;

/**
 * TODO: explain
 * TODO: make it extensible (dependency-injection)
 */
public class ProjectedVariableExtractionTools {

    public static ImmutableSet<Variable> extractProjectedVariables(IntermediateQuery query, QueryNode node) {
        ProjectedVariableExtractor extractor = new ProjectedVariableExtractorImpl(query);
        node.acceptVisitor(extractor);
        return extractor.getCollectedProjectedVariables();
    }

}
