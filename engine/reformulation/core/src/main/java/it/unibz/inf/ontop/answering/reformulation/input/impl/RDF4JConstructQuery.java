package it.unibz.inf.ontop.answering.reformulation.input.impl;

import it.unibz.inf.ontop.answering.reformulation.input.ConstructQuery;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.answering.resultset.SimpleGraphResultSet;
import it.unibz.inf.ontop.answering.reformulation.input.SPARQLQueryUtility;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.QueryParserUtil;


class RDF4JConstructQuery extends RDF4JInputQuery<SimpleGraphResultSet> implements ConstructQuery {
    private final ConstructTemplate template;

    RDF4JConstructQuery(String queryString, ParsedQuery parsedQuery) {
        super(extractSelectParsedQuery(queryString), queryString);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDF4JConstructQuery that = (RDF4JConstructQuery) o;

        if (!template.equals(that.template)) return false;
        return getParsedQuery().equals(that.getParsedQuery());
    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + getParsedQuery().hashCode();
        return result;
    }
}
