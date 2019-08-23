package it.unibz.inf.ontop.answering.reformulation.input.translation.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.InternalSparqlQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import javax.annotation.Nullable;

/**
 * Wrapper for SparqlAlgebraToDatalogTranslator to make it thread-safe
 */
public class DatalogSparqlQueryTranslator implements RDF4JInputQueryTranslator {

    private final UriTemplateMatcher uriTemplateMatcher;
    @Nullable
    private final IRIDictionary iriDictionary;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;
    private final RDF rdfFactory;

    /**
     * TODO: use Guice and retrieve the IRIDictionary by injection (not assisted, nullable)
     */
    @AssistedInject
    private DatalogSparqlQueryTranslator(@Assisted UriTemplateMatcher uriTemplateMatcher,
                                         @Nullable IRIDictionary iriDictionary,
                                         AtomFactory atomFactory, TermFactory termFactory,
                                         TypeFactory typeFactory, DatalogFactory datalogFactory,
                                         ImmutabilityTools immutabilityTools, RDF rdfFactory) {

        this.uriTemplateMatcher = uriTemplateMatcher;
        this.iriDictionary = iriDictionary;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
        this.rdfFactory = rdfFactory;
    }


    @Override
    public InternalSparqlQuery translate(ParsedQuery inputParsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        SparqlAlgebraToDatalogTranslator mutableTranslator =
                new SparqlAlgebraToDatalogTranslator(uriTemplateMatcher, iriDictionary, atomFactory, termFactory,
                        typeFactory, datalogFactory, immutabilityTools, rdfFactory);

        return mutableTranslator.translate(inputParsedQuery);
    }
}
