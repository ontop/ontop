package it.unibz.inf.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.*;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.SQLMappingParser;

import java.io.File;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private final SQLPPMappingFactory ppMappingFactory;
    private final SpecificationFactory specificationFactory;


    @Inject
    private R2RMLMappingParser(SQLPPMappingFactory ppMappingFactory, SpecificationFactory specificationFactory) {
        this.ppMappingFactory = ppMappingFactory;
        this.specificationFactory = specificationFactory;
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException, DuplicateMappingException {

        try {
            R2RMLManager r2rmlManager = new R2RMLManager(mappingFile);
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
    public SQLPPMapping parse(Model mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        R2RMLManager r2rmlManager = new R2RMLManager(new RDF4J().asGraph(mappingGraph));
        return parse(r2rmlManager);
    }

    private SQLPPMapping parse(R2RMLManager manager) throws DuplicateMappingException {
        //TODO: make the R2RMLManager simpler.
        ImmutableList<SQLPPTriplesMap> sourceMappings = manager.getMappings(manager.getModel());

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                sourceMappings.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream())
                        .flatMap(atom -> atom.getArguments().stream())
                        .filter(t -> t instanceof ImmutableFunctionalTerm)
                        .map(t -> (ImmutableFunctionalTerm) t));

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
        MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager, uriTemplateMatcher);

        return ppMappingFactory.createSQLPreProcessedMapping(sourceMappings, mappingMetadata);
    }


}
