package it.unibz.inf.ontop.materialization.impl;

import org.apache.commons.rdf.api.IRI;

final class VocabularyEntry {
    final IRI name;
    final int arity;

    VocabularyEntry(IRI predicate, int arity) {

        this.name = predicate;
        this.arity = arity;
    }

    @Override
    public String toString() {
        return name + "/" + arity;
    }

    private static final String PROPERTY_QUERY_TEMPLATE = "CONSTRUCT {?s <%$1s> ?o} WHERE {?s <%$1s> ?o}";
    private static final String CLASS_QUERY_TEMPLATE = "CONSTRUCT {?s a <%$1s>} WHERE {?s a <%$1s>}";

    private static final String SELECT_PROPERTY_QUERY_CONTEXT_TEMPLATE = "SELECT DISTINCT ?s ?o ?g WHERE {GRAPH ?g {?s <%s> ?o}}"; // Davide> TODO
    private static final String SELECT_CLASS_QUERY_CONTEXT_TEMPLATE = "SELECT DISTINCT ?s ?g WHERE {GRAPH ?g {?s a <%s>}}"; // Davide> TODO

    private static final String SELECT_PROPERTY_QUERY_TEMPLATE = "SELECT DISTINCT ?s ?o WHERE {?s <%s> ?o}"; // Davide> TODO
    private static final String SELECT_CLASS_QUERY_TEMPLATE = "SELECT DISTINCT ?s WHERE {?s a <%s>}"; // Davide> TODO

    boolean isClass() {
        return arity == 1;
    }

    String getIRIString() {
        return name.getIRIString();
    }

    String getQuery() {
        return String.format((arity == 1) ? CLASS_QUERY_TEMPLATE : PROPERTY_QUERY_TEMPLATE, name.getIRIString());
    }

//        String getQueryQuad () {
//        	return String.format((arity == 1) ? CLASS_QUERY_GRAPH : PROPERTY_QUERY_GRAPH, name.getIRIString(), name.getIRIString());
//		}

    String getSelectQuery() {
        return String.format(
                arity == 1 ? SELECT_CLASS_QUERY_TEMPLATE : SELECT_PROPERTY_QUERY_TEMPLATE,
                name.getIRIString());
    }

    String getSelectQuadQuery() {
        return String.format(
                arity == 1 ? SELECT_CLASS_QUERY_CONTEXT_TEMPLATE : SELECT_PROPERTY_QUERY_CONTEXT_TEMPLATE,
                name.getIRIString());
    }
}
