package it.unibz.inf.ontop.owlrefplatform.core.translator;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import javax.annotation.Nullable;

/**
 * Wrapper for SparqlAlgebraToDatalogTranslator to make it thread-safe
 */
public class DatalogSparqlQueryTranslator implements RDF4JInputQueryTranslator {

    private final UriTemplateMatcher uriTemplateMatcher;
    @Nullable
    private final IRIDictionary iriDictionary;

    /**
     * TODO: use Guice and retrieve the IRIDictionary by injection (not assisted, nullable)
     */
    @AssistedInject
    private DatalogSparqlQueryTranslator(@Assisted UriTemplateMatcher uriTemplateMatcher,
                                         @Nullable IRIDictionary iriDictionary) {

        this.uriTemplateMatcher = uriTemplateMatcher;
        this.iriDictionary = iriDictionary;
    }


    @Override
    public InternalSparqlQuery translate(ParsedQuery inputParsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        SparqlAlgebraToDatalogTranslator mutableTranslator =
                new SparqlAlgebraToDatalogTranslator(uriTemplateMatcher, iriDictionary);

        return mutableTranslator.translate(inputParsedQuery);
    }
}
