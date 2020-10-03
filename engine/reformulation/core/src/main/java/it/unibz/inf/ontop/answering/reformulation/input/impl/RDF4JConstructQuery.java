package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.reformulation.input.SPARQLQueryUtility;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;


class RDF4JConstructQuery extends RDF4JInputQuery<SimpleGraphResultSet> implements ConstructQuery {
    private final ConstructTemplate template;

    RDF4JConstructQuery(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        super(extractSelectParsedQuery(queryString), queryString, bindings);
        this.template = new RDF4JConstructTemplate(parsedQuery);
    }

    private static ParsedQuery extractSelectParsedQuery(String constructString) {
        String selectString = SPARQLQueryUtility.getSelectFromConstruct(constructString);
        try {
            return QueryParserUtil.parseQuery(QueryLanguage.SPARQL, selectString, null);
        } catch (MalformedQueryException e) {
            throw new IllegalStateException("Bug: bad extraction of the SELECT query from a CONSTRUCT query :"
                    + e.getMessage());
        }
    }

    @Override
    public ConstructTemplate getConstructTemplate() {
        return template;
    }

}
