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

    private static final String SELECT_PROPERTY_QUERY_CONTEXT_TEMPLATE =
            "SELECT DISTINCT ?s ?o ?g WHERE { {?s <%1$s> ?o} UNION {GRAPH ?g {?s <%1$s> ?o}} }";
    private static final String SELECT_CLASS_QUERY_CONTEXT_TEMPLATE =
            "SELECT DISTINCT ?s ?g WHERE { {?s a <%1$s>}  UNION {GRAPH ?g {?s a <%1$s>}} }";

    boolean isClass() {
        return arity == 1;
    }

    String getIRIString() {
        return name.getIRIString();
    }

    String getSelectQuery() {
        return String.format(
                arity == 1 ? SELECT_CLASS_QUERY_CONTEXT_TEMPLATE : SELECT_PROPERTY_QUERY_CONTEXT_TEMPLATE,
                name.getIRIString());
    }
}
