package it.unibz.inf.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.injection.OBDAFactoryWithException;
import it.unibz.inf.ontop.io.PrefixManager;
import it.unibz.inf.ontop.mapping.SQLMappingParser;
import it.unibz.inf.ontop.model.SQLPPMappingAxiom;
import it.unibz.inf.ontop.model.SQLPPMapping;

import java.io.File;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    private final SpecificationFactory specificationFactory;


    @Inject
    private R2RMLMappingParser(NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, SpecificationFactory specificationFactory) {
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.specificationFactory = specificationFactory;
    }


    @Override
    public SQLPPMapping parse(File mappingFile) throws InvalidMappingException, MappingIOException, DuplicateMappingException {

        try {
            R2RMLManager r2rmlManager = new R2RMLManager(mappingFile, nativeQLFactory);
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
        R2RMLManager r2rmlManager = new R2RMLManager(new RDF4J().asGraph(mappingGraph), nativeQLFactory);
        return parse(r2rmlManager);
    }

    private SQLPPMapping parse(R2RMLManager manager) throws DuplicateMappingException {
        //TODO: make the R2RMLManager simpler.
        ImmutableList<SQLPPMappingAxiom> sourceMappings = manager.getMappings(manager.getModel());

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                sourceMappings.stream()
                        .flatMap(ax -> ax.getTargetQuery().stream())
                        .flatMap(atom -> atom.getTerms().stream())
                        .filter(t -> t instanceof Function)
                        .map(t -> (Function) t));

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());
        MappingMetadata mappingMetadata = specificationFactory.createMetadata(prefixManager, uriTemplateMatcher);

        return obdaFactory.createSQLPreProcessedMapping(sourceMappings, mappingMetadata);
    }


}
