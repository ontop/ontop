package it.unibz.inf.ontop.rdf4j.predefined.parsing.impl;

import it.unibz.inf.ontop.rdf4j.predefined.parsing.PredefinedQueryConfigEntry;
import org.eclipse.rdf4j.model.IRI;

import javax.annotation.Nullable;
import java.util.Optional;

public class QueryParameterTypeImpl implements PredefinedQueryConfigEntry.QueryParameterType {

    private final PredefinedQueryConfigEntry.QueryParameterCategory category;
    @Nullable
    private final IRI datatypeIRI;

    protected QueryParameterTypeImpl(PredefinedQueryConfigEntry.QueryParameterCategory category,IRI datatypeIRI) {
        this.category = category;
        this.datatypeIRI = datatypeIRI;
    }

    protected QueryParameterTypeImpl(PredefinedQueryConfigEntry.QueryParameterCategory category) {
        this.category = category;
        this.datatypeIRI = null;
    }

    @Override
    public PredefinedQueryConfigEntry.QueryParameterCategory getCategory() {
        return category;
    }

    @Override
    public Optional<IRI> getDatatypeIRI() {
        return Optional.ofNullable(datatypeIRI);
    }
}
