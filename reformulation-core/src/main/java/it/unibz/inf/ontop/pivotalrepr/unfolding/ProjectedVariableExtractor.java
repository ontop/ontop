package it.unibz.inf.ontop.pivotalrepr.unfolding;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.Variable;
import it.unibz.inf.ontop.pivotalrepr.QueryNodeVisitor;

/**
 * TODO: explain
 */
public interface ProjectedVariableExtractor extends QueryNodeVisitor {

    /**
     * Result of the extraction
     */
    ImmutableSet<Variable> getCollectedProjectedVariables();
}
