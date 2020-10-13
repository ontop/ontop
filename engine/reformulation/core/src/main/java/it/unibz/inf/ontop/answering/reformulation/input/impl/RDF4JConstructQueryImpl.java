package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JConstructQuery;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.reformulation.input.SPARQLQueryUtility;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;


class RDF4JConstructQueryImpl extends RDF4JInputQueryImpl<SimpleGraphResultSet> implements RDF4JConstructQuery {
    private final ConstructTemplate template;

    RDF4JConstructQueryImpl(String queryString, ParsedQuery parsedQuery, BindingSet bindings) {
        this(new RDF4JConstructTemplate(parsedQuery), extractSelectParsedQuery(queryString), queryString, bindings);
    }

    private RDF4JConstructQueryImpl(ConstructTemplate template, ParsedQuery selectParsedQuery, String queryString,
                                    BindingSet bindings) {
        super(selectParsedQuery, queryString, bindings);
        this.template = template;
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

    @Override
    public RDF4JConstructQuery newBindings(BindingSet newBindings) {
        return new RDF4JConstructQueryImpl(template, parsedQuery, getInputString(), newBindings);
    }
}
