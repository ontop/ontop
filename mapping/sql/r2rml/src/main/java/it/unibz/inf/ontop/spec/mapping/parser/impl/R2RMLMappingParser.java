package it.unibz.inf.ontop.spec.mapping.parser.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import eu.optique.r2rml.api.model.impl.InvalidR2RMLMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopMappingSQLSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.MappingMetadata;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;

import java.io.File;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final RDF rdfFactory;
    private final OntopMappingSQLSettings settings;


    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory,
                               TermFactory termFactory, TypeFactory typeFactory, TargetAtomFactory targetAtomFactory,
                               RDF rdfFactory, OntopMappingSQLSettings settings) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.rdfFactory = rdfFactory;
        this.settings = settings;
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException, DuplicateMappingException {

        try {
            R2RMLManager r2rmlManager = new R2RMLManager(mappingFile, termFactory, typeFactory, targetAtomFactory,
                    rdfFactory, settings);
            return parse(r2rmlManager);
        } catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }


    @Override
    public SQLPPMapping parse(Reader reader) throws InvalidMappingException, MappingIOException, DuplicateMappingException {
        // TODO: support this
        throw new UnsupportedOperationException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public SQLPPMapping parse(Graph mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        R2RMLManager r2rmlManager = new R2RMLManager(mappingGraph, termFactory, typeFactory, targetAtomFactory,
                rdfFactory, settings);
        return parse(r2rmlManager);
    }

    private SQLPPMapping parse(R2RMLManager manager) throws DuplicateMappingException, InvalidMappingException {
        try {
            //TODO: make the R2RMLManager simpler.
            ImmutableList<SQLPPTriplesMap> sourceMappings = manager.getMappings(manager.getModel());

            //TODO: try to extract prefixes from the R2RML mappings
            PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
            MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager);

            return ppMappingFactory.createSQLPreProcessedMapping(sourceMappings, mappingMetadata);
        } catch (InvalidR2RMLMappingException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }


}
