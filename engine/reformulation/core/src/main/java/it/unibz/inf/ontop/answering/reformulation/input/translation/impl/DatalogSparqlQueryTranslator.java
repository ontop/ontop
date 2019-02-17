package it.unibz.inf.ontop.answering.reformulation.input.translation.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.InternalSparqlQuery;
import it.unibz.inf.ontop.answering.reformulation.input.translation.RDF4JInputQueryTranslator;
import it.unibz.inf.ontop.exception.OntopInvalidInputQueryException;
import it.unibz.inf.ontop.exception.OntopUnsupportedInputQueryException;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.IRIDictionary;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import javax.annotation.Nullable;

/**
 * Wrapper for SparqlAlgebraToDatalogTranslator to make it thread-safe
 */
public class DatalogSparqlQueryTranslator implements RDF4JInputQueryTranslator {

    @Nullable
    private final IRIDictionary iriDictionary;
    private final AtomFactory atomFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final DatalogFactory datalogFactory;
    private final ImmutabilityTools immutabilityTools;
    private final RDF rdfFactory;

    @Inject
    private DatalogSparqlQueryTranslator(@Nullable IRIDictionary iriDictionary,
                                         AtomFactory atomFactory, TermFactory termFactory,
                                         TypeFactory typeFactory, FunctionSymbolFactory functionSymbolFactory,
                                         DatalogFactory datalogFactory,
                                         ImmutabilityTools immutabilityTools, RDF rdfFactory) {

        this.iriDictionary = iriDictionary;
        this.atomFactory = atomFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.datalogFactory = datalogFactory;
        this.immutabilityTools = immutabilityTools;
        this.rdfFactory = rdfFactory;
    }


    @Override
    public InternalSparqlQuery translate(ParsedQuery inputParsedQuery)
            throws OntopUnsupportedInputQueryException, OntopInvalidInputQueryException {

        SparqlAlgebraToDatalogTranslator mutableTranslator =
                new SparqlAlgebraToDatalogTranslator(iriDictionary, atomFactory, termFactory,
                        typeFactory, functionSymbolFactory, datalogFactory, immutabilityTools, rdfFactory);

        return mutableTranslator.translate(inputParsedQuery);
    }
}
