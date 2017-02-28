package it.unibz.inf.ontop.r2rml;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.MappingFactory;
import it.unibz.inf.ontop.mapping.MappingMetadata;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.UriTemplateMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.ontology.impl.OntologyVocabularyImpl;
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
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDAModel;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * High-level class that implements the MappingParser interface for R2RML.
 */
public class R2RMLMappingParser implements SQLMappingParser {

    private final NativeQueryLanguageComponentFactory nativeQLFactory;
    private final OBDAFactoryWithException obdaFactory;
    private final MappingFactory mappingFactory;


    @Inject
    private R2RMLMappingParser(NativeQueryLanguageComponentFactory nativeQLFactory,
                               OBDAFactoryWithException obdaFactory, MappingFactory mappingFactory){
        this.nativeQLFactory = nativeQLFactory;
        this.obdaFactory = obdaFactory;
        this.mappingFactory = mappingFactory;
    }


    @Override
    public OBDAModel parse(File mappingFile) throws InvalidMappingException, MappingIOException, DuplicateMappingException {

        try {
            R2RMLManager r2rmlManager = new R2RMLManager(mappingFile, nativeQLFactory);
            return parse(r2rmlManager);
        } catch (RDFParseException | RDFHandlerException e) {
            throw new InvalidMappingException(e.getMessage());
        }
    }

//        R2RMLManager r2rmlManager;
//
//        if (mappingFile != null) {
//        try {
//            r2rmlManager = new R2RMLManager(mappingFile, nativeQLFactory);
//        } catch (RDFParseException | RDFHandlerException e) {
//            throw new InvalidDataSourceException(e.getMessage());
//        }
//    }
//
//        else if (mappingGraph != null){
//            r2rmlManager = new R2RMLManager(new RDF4J().asGraph(mappingGraph), nativeQLFactory);
//        }
//        else
//            throw new RuntimeException("Internal inconsistency. A mappingFile or a mappingGraph should be defined.");

    @Override
    public OBDAModel parse(Reader reader) throws InvalidMappingException, MappingIOException, DuplicateMappingException {
        // TODO: support this
        throw new UnsupportedOperationException("The R2RMLMappingParser does not support" +
                "yet the Reader interface.");
    }

    @Override
    public OBDAModel parse(Model mappingGraph) throws InvalidMappingException, DuplicateMappingException {
        R2RMLManager r2rmlManager = new R2RMLManager(new RDF4J().asGraph(mappingGraph), nativeQLFactory);
        return parse(r2rmlManager);
    }

    private OBDAModel parse(R2RMLManager manager) throws DuplicateMappingException {
        //TODO: make the R2RMLManager simpler.
        ImmutableList<OBDAMappingAxiom> sourceMappings = manager.getMappings(manager.getModel());

        UriTemplateMatcher uriTemplateMatcher = UriTemplateMatcher.create(
                sourceMappings.stream()
                        .flatMap(ax -> ax.getTargetQuery().stream())
                        .flatMap(atom -> atom.getTerms().stream())
                        .filter(t -> t instanceof Function)
                        .map(t -> (Function) t));

        //TODO: try to extract prefixes from the R2RML mappings
        PrefixManager prefixManager = mappingFactory.create(ImmutableMap.of());
        MappingMetadata mappingMetadata = mappingFactory.create(prefixManager, uriTemplateMatcher);

        return obdaFactory.createOBDAModel(sourceMappings, mappingMetadata);
    }


}
