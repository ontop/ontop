package it.unibz.inf.ontop.answering.reformulation.input;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.rdf.api.IRI;

public interface DescribeQuery extends GraphSPARQLQuery {

    /**
     * May return a SELECT query for fetching all the resources to describe
     *
     * Returns empty when only constants
     */
    SelectQuery getSelectQuery();

    /**
     * Restriction: description of blank nodes is not supported.
     */
    ImmutableCollection<ConstructQuery> computeConstructQueries(ImmutableSet<IRI> resourcesToDescribe);


}
