package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.answering.resultset.TupleResultSet;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.iq.node.ConstructionNode;

import java.sql.ResultSet;
import java.util.Optional;

/**
 * Implementation of TupleResultSet for SQL queries, in the case where the IRI construction is post-processed rather than
 * delegated to the source engine (e.g. when using Calcite as a SQL query generator).
 */
public class PostProcessedIriSQLTupleResultSet extends AbstractSQLTupleResultSet implements TupleResultSet{

    private final ConstructionNode constructionNode;

    protected PostProcessedIriSQLTupleResultSet(ResultSet rs, ImmutableList<String> signature, ConstructionNode constructionNode) {
        super(rs, signature);
        this.constructionNode = constructionNode;
    }

    @Override
    protected PostProcessedIriSQLBindingSet readCurrentRow() throws OntopConnectionException {
        return new PostProcessedIriSQLBindingSet(rs, signature, constructionNode);
    }
}
